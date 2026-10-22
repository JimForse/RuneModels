package rw.modden.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Vanilla armor on the bones of a geo model: each piece is fitted to the cubes of the bone with the same name */
public class ArmorLayer extends GeoRenderLayer<Animatable> {
    private static final Map<String, Identifier> TEXTURES = new ConcurrentHashMap<>();
    private final EntityRenderer entityRenderer;
    private final BipedEntityModel<LivingEntity> inner, outer;
    private final SpriteAtlasTexture trims;

    /** Size and centre of the vanilla body part (without armor inflation), in pixels, relative to the part's pivot */
    private enum Shape {
        HEAD(8, 8, 8, 0, -4, 0), BODY(8, 12, 4, 0, 6, 0),
        RIGHT_ARM(4, 12, 4, -1, 4, 0), LEFT_ARM(4, 12, 4, 1, 4, 0), LEG(4, 12, 4, 0, 6, 0);

        final Vector3f size, center;

        Shape(float sx, float sy, float sz, float cx, float cy, float cz) {
            this.size = new Vector3f(sx, sy, sz);
            this.center = new Vector3f(cx, cy, cz);
        }
    }

    public ArmorLayer(EntityRenderer renderer, EntityRendererFactory.Context ctx) {
        super(renderer);
        this.entityRenderer = renderer;
        this.inner = new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR));
        this.outer = new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR));
        this.trims = ctx.getModelManager().getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    @Override
    public void renderForBone(MatrixStack matrices, Animatable animatable, GeoBone bone, RenderLayer renderType,
                              VertexConsumerProvider buffers, VertexConsumer buffer, float tickDelta, int light, int overlay) {
        if (!(entityRenderer.getCurrentEntity() instanceof LivingEntity entity)) return;
        if (Bones.is(bone, Bones.HEAD)) {
            piece(matrices, entity, bone, EquipmentSlot.HEAD, Shape.HEAD, buffers, light, outer.head, outer.hat);
        } else if (Bones.is(bone, Bones.BODY)) {
            piece(matrices, entity, bone, EquipmentSlot.CHEST, Shape.BODY, buffers, light, outer.body);
            piece(matrices, entity, bone, EquipmentSlot.LEGS, Shape.BODY, buffers, light, inner.body);
        } else if (Bones.is(bone, Bones.RIGHT_ARM)) {
            piece(matrices, entity, bone, EquipmentSlot.CHEST, Shape.RIGHT_ARM, buffers, light, outer.rightArm);
        } else if (Bones.is(bone, Bones.LEFT_ARM)) {
            piece(matrices, entity, bone, EquipmentSlot.CHEST, Shape.LEFT_ARM, buffers, light, outer.leftArm);
        } else if (Bones.is(bone, Bones.RIGHT_LEG)) {
            piece(matrices, entity, bone, EquipmentSlot.LEGS, Shape.LEG, buffers, light, inner.rightLeg);
            piece(matrices, entity, bone, EquipmentSlot.FEET, Shape.LEG, buffers, light, outer.rightLeg);
        } else if (Bones.is(bone, Bones.LEFT_LEG)) {
            piece(matrices, entity, bone, EquipmentSlot.LEGS, Shape.LEG, buffers, light, inner.leftLeg);
            piece(matrices, entity, bone, EquipmentSlot.FEET, Shape.LEG, buffers, light, outer.leftLeg);
        } else return;
        buffers.getBuffer(renderType);
    }

    private void piece(MatrixStack matrices, LivingEntity entity, GeoBone bone, EquipmentSlot slot, Shape shape,
                       VertexConsumerProvider buffers, int light, ModelPart... parts) {
        ItemStack stack = entity.getEquippedStack(slot);
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getSlotType() != slot) return;
        boolean legs = slot == EquipmentSlot.LEGS;
        ArmorTrim trim = ArmorTrim.getTrim(entity.getWorld().getRegistryManager(), stack).orElse(null);

        matrices.push();
        fit(matrices, bone, shape);
        for (ModelPart part: parts) {
            part.setPivot(0, 0, 0);
            part.setAngles(0, 0, 0);
            part.visible = true;
            if (armor instanceof DyeableArmorItem dyeable) {
                int color = dyeable.getColor(stack);
                layer(part, matrices, buffers, light, texture(armor, legs, null),
                        (color >> 16 & 255)/255f, (color >> 8 & 255)/255f, (color & 255)/255f);
                layer(part, matrices, buffers, light, texture(armor, legs, "overlay"), 1, 1, 1);
            } else {
                layer(part, matrices, buffers, light, texture(armor, legs, null), 1, 1, 1);
            }
            if (trim != null) {
                Sprite sprite = trims.getSprite(legs?trim.getLeggingsModelId(armor.getMaterial()):trim.getGenericModelId(armor.getMaterial()));
                part.render(matrices, sprite.getTextureSpecificVertexConsumer(buffers.getBuffer(TexturedRenderLayers.getArmorTrims())),
                        light, OverlayTexture.DEFAULT_UV);
            }
            if (stack.hasGlint()) part.render(matrices, buffers.getBuffer(RenderLayer.getArmorEntityGlint()), light, OverlayTexture.DEFAULT_UV);
        }
        matrices.pop();
    }

    /**
     * Moves and stretches the vanilla part onto the bone: its centre goes to the centre of the bone's own cubes,
     * its size becomes their size. A bone without cubes gets the part at its pivot, at vanilla size
     */
    private static void fit(MatrixStack matrices, GeoBone bone, Shape shape) {
        Vector3f min = new Vector3f(Float.MAX_VALUE), max = new Vector3f(-Float.MAX_VALUE);
        for (GeoCube cube: bone.getCubes()) {
            Vector3f cubeMin = new Vector3f(Float.MAX_VALUE), cubeMax = new Vector3f(-Float.MAX_VALUE);
            for (GeoQuad quad: cube.quads()) {
                for (GeoVertex vertex: quad.vertices()) {
                    cubeMin.min(vertex.position());
                    cubeMax.max(vertex.position());
                }
            }
            float inflate = (float) cube.inflate();
            min.min(cubeMin.add(inflate, inflate, inflate));
            max.max(cubeMax.sub(inflate, inflate, inflate));
        }
        if (min.x > max.x) {
            RenderUtils.translateToPivotPoint(matrices, bone);
            matrices.scale(-1, -1, 1);
            return;
        }
        matrices.translate((min.x + max.x)/2, (min.y + max.y)/2, (min.z + max.z)/2);
        matrices.scale(-1, -1, 1);
        matrices.scale((max.x - min.x)*16/shape.size.x, (max.y - min.y)*16/shape.size.y, (max.z - min.z)*16/shape.size.z);
        matrices.translate(-shape.center.x/16, -shape.center.y/16, -shape.center.z/16);
    }

    private static void layer(ModelPart part, MatrixStack matrices, VertexConsumerProvider buffers, int light,
                              Identifier texture, float r, float g, float b) {
        part.render(matrices, buffers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV, r, g, b, 1);
    }

    /** Vanilla armor texture. A material named "mymod:steel" is looked up in the "mymod" namespace */
    private static Identifier texture(ArmorItem armor, boolean legs, String overlay) {
        String key = armor.getMaterial().getName() + (legs?"_layer_2":"_layer_1") + (overlay == null?"":"_" + overlay);
        return TEXTURES.computeIfAbsent(key, k -> {
            int colon = k.indexOf(':');
            String namespace = colon < 0?"minecraft":k.substring(0, colon);
            return new Identifier(namespace, "textures/models/armor/" + k.substring(colon + 1) + ".png");
        });
    }
}
