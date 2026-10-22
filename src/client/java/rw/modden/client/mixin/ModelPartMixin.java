package rw.modden.client.mixin;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.client.render.PartNames;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    /** Vanilla models take their parts by name when they are built: remember which name each part has */
    @Inject(method = "getChild", at = @At("RETURN"))
    private void runemodels$rememberName(String name, CallbackInfoReturnable<ModelPart> cir) {
        PartNames.remember(cir.getReturnValue(), name);
    }
}
