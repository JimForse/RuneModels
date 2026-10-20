package rw.modden.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.ProfileSync;
import rw.modden.profile.ProfileTags;

@Mixin(Entity.class)
public abstract class EntityTagMixin {
    @Inject(method = "addCommandTag", at = @At("RETURN"))
    private void runemodels$added(String tag, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && tag.startsWith(ProfileTags.PREFIX)) {
            ProfileSync.onTagsChanged((Entity) (Object) this);
        }
    }

    @Inject(method = "removeScoreboardTag", at = @At("RETURN"))
    private void runemodels$removed(String tag, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && tag.startsWith(ProfileTags.PREFIX)) {
            ProfileSync.onTagsChanged((Entity) (Object) this);
        }
    }
}