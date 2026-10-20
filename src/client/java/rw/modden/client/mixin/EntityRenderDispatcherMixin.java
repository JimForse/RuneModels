package rw.modden.client.mixin;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.client.render.ClientProfiles;
import rw.modden.client.render.RendererHolder;
import rw.modden.client.render.EntityRenderer;
import rw.modden.profile.Profile;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void runemodels$override(T entity,
                                                        CallbackInfoReturnable<net.minecraft.client.render.entity.EntityRenderer<? super T>> cir) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null) return;
        EntityRenderer renderer = RendererHolder.get();
        if (renderer != null) cir.setReturnValue((net.minecraft.client.render.entity.EntityRenderer<? super T>) (net.minecraft.client.render.entity.EntityRenderer<?>) renderer);
    }
}