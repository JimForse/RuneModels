package rw.modden.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.client.render.ClientProfiles;
import rw.modden.client.render.FirstPersonArm;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    /** Texture-only profiles: the vanilla arm and sleeve get the profile texture instead of the skin */
    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTexture()Lnet/minecraft/util/Identifier;"))
    private Identifier runemodels$armTexture(AbstractClientPlayerEntity player, Operation<Identifier> original) {
        Profile profile = ClientProfiles.getUsable(player.getId());
        if (profile == null || profile.model() != null) return original.call(player);
        Identifier id = Names.texture(profile.texture());
        return id != null?id:original.call(player);
    }

    /** Model profiles: the arm bone from the geo model replaces the vanilla arm. Runs after the game has posed the arm */
    @Inject(method = "renderArm", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            ordinal = 0), cancellable = true)
    private void runemodels$modelArm(MatrixStack matrices, VertexConsumerProvider buffers, int light,
                                     AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        boolean right = arm == ((PlayerEntityRenderer) (Object) this).getModel().rightArm;
        if (FirstPersonArm.render(player, arm, right, matrices, buffers, light)) ci.cancel();
    }
}
