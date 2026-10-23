package rw.modden.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;

/** Stores the render context and lazily creates our renderer */
public final class RendererHolder {
    private static EntityRendererFactory.Context context;
    private static EntityRenderer renderer;

    private RendererHolder() {}

    /** Called whenever renderers are re-initialized: the old renderer is no longer valid */
    public static void setContext(EntityRendererFactory.Context ctx) {
        context = ctx;
        renderer = null;
    }
    public static EntityRenderer get() {
        if (renderer == null && context != null) renderer = new EntityRenderer(context);
        return renderer;
    }

    /** The entity is gone: its animation state is not needed any more */
    public static void forget(int entityId) {
        if (renderer != null) renderer.getAnimatable().forget(entityId);
    }
    public static void forgetAll() {
        if (renderer != null) renderer.getAnimatable().forgetAll();
    }
}
