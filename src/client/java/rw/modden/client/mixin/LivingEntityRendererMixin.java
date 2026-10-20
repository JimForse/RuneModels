package rw.modden.client.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.client.render.ClientProfiles;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected M model;
    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void runemodels$textureOnly(T entity, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() != null || profile.texture() == null) return;
        Identifier id = Names.texture(profile.texture());
        if (id == null) return;

        if (translucent) cir.setReturnValue(RenderLayer.getItemEntityTranslucentCull(id));
        else if (showBody) cir.setReturnValue(this.model.getLayer(id));
        else cir.setReturnValue(showOutline?RenderLayer.getOutline(id):null);
    }
}