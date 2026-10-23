package rw.modden.client.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

public final class RendererInvokers {
    private RendererInvokers() {}

    @Mixin(EntityRenderer.class)
    public interface Base {
        @Accessor("shadowRadius") float runemodels$getShadowRadius();
        @Accessor("shadowOpacity") float runemodels$getShadowOpacity();
        @Invoker("renderLabelIfPresent") void runemodels$renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices,
                                                                              VertexConsumerProvider buffers, int light);
    }

    @Mixin(LivingEntityRenderer.class)
    public interface Living {
        @Invoker("getHandSwingProgress") float runemodels$getHandSwingProgress(LivingEntity entity, float tickDelta);
        @Invoker("getAnimationProgress") float runemodels$getAnimationProgress(LivingEntity entity, float tickDelta);
        @Invoker("scale") void runemodels$scale(LivingEntity entity, MatrixStack matrices, float tickDelta);
        @Invoker("setupTransforms") void runemodels$setupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta);
    }

    @Mixin(PlayerEntityRenderer.class)
    public interface Player {
        @Invoker("setModelPose") void runemodels$setModelPose(AbstractClientPlayerEntity player);
    }

    @Mixin(AnimalModel.class)
    public interface Animal {
        @Invoker("getHeadParts") Iterable<ModelPart> runemodels$getHeadParts();
        @Invoker("getBodyParts") Iterable<ModelPart> runemodels$getBodyParts();
    }

    @Mixin(StuckObjectsFeatureRenderer.class)
    public interface Stuck {
        @Invoker("renderObject") void runemodels$renderObject(MatrixStack matrices, VertexConsumerProvider buffers, int light, Entity entity,
                                                              float directionX, float directionY, float directionZ, float tickDelta);
    }
}
