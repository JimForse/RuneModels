package rw.modden.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rw.modden.client.render.ClientProfiles;
import rw.modden.client.render.EntityRenderer;
import rw.modden.client.render.RendererHolder;
import rw.modden.profile.Profile;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    /**
     * Swaps the renderer only when the entity is drawn in the world.
     * Other callers of getRenderer (first-person hand, light, culling) keep the vanilla renderer,
     * because some of them cast it to a specific type (e.g. PlayerEntityRenderer)
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/render/entity/EntityRenderer;"))
    private net.minecraft.client.render.entity.EntityRenderer<?> runemodels$override(EntityRenderDispatcher dispatcher, Entity entity,
                                                                                    Operation<net.minecraft.client.render.entity.EntityRenderer<?>> original) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile != null && profile.model() != null) {
            EntityRenderer renderer = RendererHolder.get();
            if (renderer != null) return renderer;
        }
        return original.call(dispatcher, entity);
    }

    /** The vanilla check first; an entity with a tall model is also drawn when only its model is on screen */
    @WrapOperation(method = "shouldRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderer;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"))
    private boolean runemodels$modelBounds(net.minecraft.client.render.entity.EntityRenderer<?> renderer, Entity entity, Frustum frustum,
                                           double x, double y, double z, Operation<Boolean> original) {
        return original.call(renderer, entity, frustum, x, y, z) || EntityRenderer.isModelVisible(entity, frustum, x, y, z);
    }
}
