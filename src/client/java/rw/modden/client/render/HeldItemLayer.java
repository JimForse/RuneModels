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
        String name = bone.getName().replace("_", "").toLowerCase();
        Arm side = name.equals("rightarm")?Arm.RIGHT:name.equals("leftarm")?Arm.LEFT:null;
        if (side == null) return;
        ItemStack stack = entity.getMainArm() == side?entity.getMainHandStack():entity.getOffHandStack();
        if (stack.isEmpty()) return;
        boolean left = side == Arm.LEFT;

        matrices.push();
        RenderUtils.translateToPivotPoint(matrices, bone);
        matrices.scale(-1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate((left?-1:1)/16f, 0.125f, -0.625f);
        MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer().renderItem(entity, stack,
                left?ModelTransformationMode.THIRD_PERSON_LEFT_HAND:ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                left, matrices, buffers, light);
        matrices.pop();
        buffers.getBuffer(renderType);
    }
}
