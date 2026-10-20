package rw.modden.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import rw.modden.profile.Profile;
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
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider buffers, int light) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null) return;
        runeModel.setCurrent(profile);
        super.render(entity, yaw, tickDelta, matrices, buffers, light);
    }
}