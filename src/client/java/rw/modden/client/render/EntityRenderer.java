package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rw.modden.client.mixin.RendererInvokers;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.loading.json.raw.ModelProperties;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/** A single renderer for all creatures with a profile */
public class EntityRenderer extends GeoReplacedEntityRenderer<Entity, Animatable> {
    private static final Color INVISIBLE = Color.ofRGBA(1f, 1f, 1f, 0f);
    private final GeoModel runeModel;

    public EntityRenderer(EntityRendererFactory.Context ctx) {
        this(ctx, new GeoModel());
    }

    private EntityRenderer(EntityRendererFactory.Context ctx, GeoModel model) {
        super(ctx, model, new Animatable(model));
        this.runeModel = model;
        addRenderLayer(new ArmorLayer(this, ctx));
        addRenderLayer(new HeldItemLayer(this));
        addRenderLayer(new StuckObjectsLayer(this, ctx));
    }

    /** The renderer vanilla uses for this entity: the dispatcher swaps in ours only for drawing the entity itself */
    private static net.minecraft.client.render.entity.EntityRenderer<? super Entity> vanilla(Entity entity) {
        return MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider buffers, int light) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null) return;
        runeModel.setCurrent(profile);
        // The dispatcher draws the shadow right after this method, with the size of the renderer it called
        RendererInvokers.Base base = (RendererInvokers.Base) vanilla(entity);
        shadowRadius = base.runemodels$getShadowRadius();
        shadowOpacity = base.runemodels$getShadowOpacity();
        super.render(entity, yaw, tickDelta, matrices, buffers, light);
    }

    /** For example, a sneaking player is drawn a little lower */
    @Override
    public Vec3d getPositionOffset(Entity entity, float tickDelta) {
        return vanilla(entity).getPositionOffset(entity, tickDelta);
    }

    /** The name the vanilla way: for players it also shows the scoreboard line under the name */
    @Override
    protected void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider buffers, int light) {
        ((RendererInvokers.Base) vanilla(entity)).runemodels$renderLabelIfPresent(entity, text, matrices, buffers, light);
    }

    /**
     * An invisible entity: its model is drawn fully transparent instead of being skipped,
     * so that its armor, held items and glowing outline stay visible, like in vanilla
     */
    @Override
    public Color getRenderColor(Animatable animatable, float tickDelta, int light) {
        PlayerEntity viewer = MinecraftClient.getInstance().player;
        if (currentEntity != null && viewer != null && currentEntity.isInvisibleTo(viewer)) return INVISIBLE;
        return super.getRenderColor(animatable, tickDelta, light);
    }

    /** Vanilla whole-body rotation instead of GeckoLib's: it also knows elytra flight, swimming and mob-specific poses */
    @Override
    protected void applyRotations(Animatable animatable, MatrixStack matrices, float ageInTicks, float rotationYaw, float tickDelta) {
        if (currentEntity instanceof LivingEntity living && vanilla(living) instanceof LivingEntityRenderer<?, ?> renderer)
            ((RendererInvokers.Living) renderer).runemodels$setupTransforms(living, matrices, ageInTicks, rotationYaw, tickDelta);
        else super.applyRotations(animatable, matrices, ageInTicks, rotationYaw, tickDelta);
    }

    /** The same size as the vanilla entity: players are drawn at 15/16, some babies are smaller, ghasts are bigger... */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, MatrixStack matrices, Animatable animatable,
                                    BakedGeoModel model, boolean isReRender, float tickDelta, int light, int overlay) {
        super.scaleModelForRender(widthScale, heightScale, matrices, animatable, model, isReRender, tickDelta, light, overlay);
        if (!isReRender && currentEntity instanceof LivingEntity living && vanilla(living) instanceof LivingEntityRenderer<?, ?> renderer)
            ((RendererInvokers.Living) renderer).runemodels$scale(living, matrices, tickDelta);
    }

    /**
     * A tall model can stick out of the vanilla bounding box, so the entity is also drawn when only its model is on screen.
     * Uses the visible bounds Blockbench writes into the model file
     */
    public static boolean isModelVisible(Entity entity, Frustum frustum, double x, double y, double z) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null || !entity.shouldRender(x, y, z)) return false;
        BakedGeoModel model = GeckoLibCache.getBakedModels().get(Names.model(profile.model()));
        ModelProperties properties = model == null?null:model.properties();
        if (properties == null || properties.visibleBoundsWidth() == null || properties.visibleBoundsHeight() == null) return false;
        double width = properties.visibleBoundsWidth(), height = properties.visibleBoundsHeight();
        double[] offset = properties.visibleBoundsOffset();
        double centerY = entity.getY() + (offset != null && offset.length > 1?offset[1]:0);
        return frustum.isVisible(new Box(entity.getX() - width/2, centerY - height/2, entity.getZ() - width/2,
                entity.getX() + width/2, centerY + height/2, entity.getZ() + width/2));
    }
}
