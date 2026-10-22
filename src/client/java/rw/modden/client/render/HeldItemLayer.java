package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

/** Items in the hands of a geo model: drawn on the arm bones the same way vanilla draws them on the arms */
public class HeldItemLayer extends GeoRenderLayer<Animatable> {
    private final EntityRenderer entityRenderer;

    public HeldItemLayer(EntityRenderer renderer) {
        super(renderer);
        this.entityRenderer = renderer;
    }

    @Override
    public void renderForBone(MatrixStack matrices, Animatable animatable, GeoBone bone, RenderLayer renderType,
                              VertexConsumerProvider buffers, VertexConsumer buffer, float tickDelta, int light, int overlay) {
        if (!(entityRenderer.getCurrentEntity() instanceof LivingEntity entity)) return;
        Arm side = sideOf(bone, animatable);
        if (side == null) return;
        ItemStack stack = entity.getMainArm() == side?entity.getMainHandStack():entity.getOffHandStack();
        if (stack.isEmpty()) return;
        boolean left = side == Arm.LEFT;
        boolean itemBone = Bones.is(bone, Bones.RIGHT_ITEM) || Bones.is(bone, Bones.LEFT_ITEM);

        matrices.push();
        RenderUtils.translateToPivotPoint(matrices, bone);
        matrices.scale(-1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        if (!itemBone) matrices.translate((left?-1:1)/16f, 0.125f, -0.625f);
        MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer().renderItem(entity, stack,
                left?ModelTransformationMode.THIRD_PERSON_LEFT_HAND:ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                left, matrices, buffers, light);
        matrices.pop();
        buffers.getBuffer(renderType);
    }

    /** Which hand this bone holds an item for: the item bone if the model has one, otherwise the arm bone */
    private Arm sideOf(GeoBone bone, Animatable animatable) {
        if (Bones.is(bone, Bones.RIGHT_ITEM)) return Arm.RIGHT;
        if (Bones.is(bone, Bones.LEFT_ITEM)) return Arm.LEFT;
        boolean right = Bones.is(bone, Bones.RIGHT_ARM);
        if (!right && !Bones.is(bone, Bones.LEFT_ARM)) return null;
        BakedGeoModel model = getDefaultBakedModel(animatable);
        if (Bones.find(model.topLevelBones(), right?Bones.RIGHT_ITEM:Bones.LEFT_ITEM) != null) return null;
        return right?Arm.RIGHT:Arm.LEFT;
    }
}
