package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import rw.modden.client.mixin.RendererInvokers;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/** A single renderer for all creatures with a profile */
public class EntityRenderer extends GeoReplacedEntityRenderer<Entity, Animatable> {
    private final GeoModel runeModel;

    public EntityRenderer(EntityRendererFactory.Context ctx) {
        this(ctx, new GeoModel());
    }

    private EntityRenderer(EntityRendererFactory.Context ctx, GeoModel model) {
        super(ctx, model, new Animatable(model));
        this.runeModel = model;
        addRenderLayer(new ArmorLayer(this, ctx));
        addRenderLayer(new HeldItemLayer(this));
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider buffers, int light) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null) return;
        runeModel.setCurrent(profile);
        super.render(entity, yaw, tickDelta, matrices, buffers, light);
    }

    /** The same size as the vanilla entity: players are drawn at 15/16, some babies are smaller, ghasts are bigger... */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, MatrixStack matrices, Animatable animatable,
                                    BakedGeoModel model, boolean isReRender, float tickDelta, int light, int overlay) {
        super.scaleModelForRender(widthScale, heightScale, matrices, animatable, model, isReRender, tickDelta, light, overlay);
        if (!isReRender && currentEntity instanceof LivingEntity living
                && MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(living) instanceof LivingEntityRenderer<?, ?> vanilla)
            ((RendererInvokers.Living) vanilla).runemodels$scale(living, matrices, tickDelta);
    }
}
