package rw.modden.client.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

public final class RendererInvokers {
    private RendererInvokers() {}

    @Mixin(LivingEntityRenderer.class)
    public interface Living {
        @Invoker("getHandSwingProgress") float runemodels$getHandSwingProgress(LivingEntity entity, float tickDelta);
        @Invoker("getAnimationProgress") float runemodels$getAnimationProgress(LivingEntity entity, float tickDelta);
        @Invoker("scale") void runemodels$scale(LivingEntity entity, MatrixStack matrices, float tickDelta);
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
}
