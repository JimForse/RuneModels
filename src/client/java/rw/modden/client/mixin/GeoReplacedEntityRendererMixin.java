package rw.modden.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rw.modden.client.render.EntityRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

@Mixin(value = GeoReplacedEntityRenderer.class, remap = false)
public class GeoReplacedEntityRendererMixin {
    /**
     * GeckoLib skips the whole model of an invisible entity, together with its armor and held items.
     * Vanilla still draws those, so our renderer always goes on and makes the model itself transparent (EntityRenderer.getRenderColor).
     * require = 0: if a GeckoLib update changes this method, invisible entities just lose their armor again instead of crashing the game
     */
    @WrapOperation(method = "actuallyRender", require = 0, at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/entity/Entity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean runemodels$keepLayers(Entity entity, PlayerEntity player, Operation<Boolean> original) {
        return !((Object) this instanceof EntityRenderer) && original.call(entity, player);
    }
}
