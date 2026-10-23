package rw.modden.client.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckStingersFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;
import rw.modden.client.mixin.RendererInvokers;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.List;

/** Arrows and bee stingers stuck in a player, spread over the cubes of the geo model the way vanilla spreads them over its parts */
public class StuckObjectsLayer extends GeoRenderLayer<Animatable> {
    private final EntityRenderer entityRenderer;
    private final RendererInvokers.Stuck arrows, stingers;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public StuckObjectsLayer(EntityRenderer renderer, EntityRendererFactory.Context ctx) {
        super(renderer);
        this.entityRenderer = renderer;
        // Only their "draw one object" part is used, and it never touches the parent renderer, so none is given
        this.arrows = (RendererInvokers.Stuck) new StuckArrowsFeatureRenderer(ctx, null);
        this.stingers = (RendererInvokers.Stuck) new StuckStingersFeatureRenderer(null);
    }

    @Override
    public void renderForBone(MatrixStack matrices, Animatable animatable, GeoBone bone, RenderLayer renderType,
                              VertexConsumerProvider buffers, VertexConsumer buffer, float tickDelta, int light, int overlay) {
        if (!(entityRenderer.getCurrentEntity() instanceof AbstractClientPlayerEntity player) || bone.getCubes().isEmpty()) return;
        int arrowCount = player.getStuckArrowCount(), stingerCount = player.getStingerCount();
        if (arrowCount + stingerCount == 0) return;
        List<GeoBone> bones = withCubes(getDefaultBakedModel(animatable).topLevelBones(), new ArrayList<>());
        render(matrices, player, bone, bones, arrows, arrowCount, buffers, light, tickDelta);
        render(matrices, player, bone, bones, stingers, stingerCount, buffers, light, tickDelta);
        buffers.getBuffer(renderType);
    }

    /** The same random sequence every frame (seeded with the entity id), so the objects stay where they are */
    private static void render(MatrixStack matrices, AbstractClientPlayerEntity player, GeoBone bone, List<GeoBone> bones,
                               RendererInvokers.Stuck renderer, int count, VertexConsumerProvider buffers, int light, float tickDelta) {
        Random random = Random.create(player.getId());
        for (int i=0;i<count;i++) {
            GeoBone target = bones.get(random.nextInt(bones.size()));
            GeoCube cube = target.getCubes().get(random.nextInt(target.getCubes().size()));
            float x = random.nextFloat(), y = random.nextFloat(), z = random.nextFloat();
            if (target != bone) continue;

            Vector3f min = new Vector3f(Float.MAX_VALUE), max = new Vector3f(-Float.MAX_VALUE);
            for (GeoQuad quad: cube.quads()) {
                for (GeoVertex vertex: quad.vertices()) {
                    min.min(vertex.position());
                    max.max(vertex.position());
                }
            }
            matrices.push();
            matrices.translate(MathHelper.lerp(x, min.x, max.x), MathHelper.lerp(y, min.y, max.y), MathHelper.lerp(z, min.z, max.z));
            matrices.scale(-1, -1, 1);
            renderer.runemodels$renderObject(matrices, buffers, light, player, -(x*2 - 1), -(y*2 - 1), -(z*2 - 1), tickDelta);
            matrices.pop();
        }
    }

    private static List<GeoBone> withCubes(List<GeoBone> bones, List<GeoBone> out) {
        for (GeoBone bone: bones) {
            if (!bone.getCubes().isEmpty()) out.add(bone);
            withCubes(bone.getChildBones(), out);
        }
        return out;
    }
}
