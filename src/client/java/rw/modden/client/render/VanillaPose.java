package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import rw.modden.RuneModels;
import rw.modden.client.mixin.RendererInvokers;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.state.BoneSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** The vanilla pose of an entity, taken from its own vanilla model and copied onto the geo bones with the same names */
public final class VanillaPose {
    private static final Set<EntityType<?>> FAILED = ConcurrentHashMap.newKeySet();

    private VanillaPose() {}

    /** Bones moved by the current custom animation are skipped: the custom animation wins */
    @SuppressWarnings("unchecked")
    public static void apply(LivingEntity entity, float tickDelta, BakedGeoModel model, Set<String> animated) {
        if (FAILED.contains(entity.getType())) return;
        if (!(MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> renderer)) return;
        EntityModel<LivingEntity> vanilla = (EntityModel<LivingEntity>) renderer.getModel();
        try {
            pose(renderer, vanilla, entity, tickDelta);
        } catch (RuntimeException e) {
            // A model from another mod may expect to be posed only by its own renderer: then that type keeps its custom pose only
            FAILED.add(entity.getType());
            RuneModels.LOGGER.warn("RuneModels: cannot take the vanilla pose of {}, its model will not follow vanilla animations",
                    entity.getType(), e);
            return;
        }
        Set<String> actions = actionParts(vanilla, entity);
        for (ModelPart part: parts(vanilla)) {
            String name = PartNames.get(part);
            if (name == null) continue;
            GeoBone bone = Bones.find(model.topLevelBones(), name.replace("_", ""));
            if (bone != null && (!animated.contains(bone.getName()) || actions.contains(name))) copy(part, bone);
        }
    }

    /**
     * Parts busy with a vanilla action: attack swing, shield, bow, crossbow, spyglass...
     * The animation sets only describe idle / walk / run, so on these parts vanilla wins even over a custom animation
     */
    private static Set<String> actionParts(EntityModel<LivingEntity> model, LivingEntity entity) {
        if (!(model instanceof BipedEntityModel<?> biped)) return Set.of();
        Set<String> parts = new HashSet<>();
        if (biped.handSwingProgress > 0) parts.add(swingArm(entity) == Arm.RIGHT?"right_arm":"left_arm");
        if (biped.rightArmPose.isTwoHanded() || biped.leftArmPose.isTwoHanded()) {
            parts.add("right_arm");
            parts.add("left_arm");
        } else {
            if (busy(biped.rightArmPose)) parts.add("right_arm");
            if (busy(biped.leftArmPose)) parts.add("left_arm");
        }
        return parts;
    }

    private static boolean busy(BipedEntityModel.ArmPose pose) {
        return pose != BipedEntityModel.ArmPose.EMPTY && pose != BipedEntityModel.ArmPose.ITEM;
    }

    /** The arm that swings: the main arm, or the other one if the swing was made with the off hand */
    private static Arm swingArm(LivingEntity entity) {
        Arm main = entity.getMainArm();
        return entity.preferredHand == Hand.MAIN_HAND?main:main.getOpposite();
    }

    /** The same preparation LivingEntityRenderer.render does before drawing the model */
    private static void pose(LivingEntityRenderer<?, ?> renderer, EntityModel<LivingEntity> model, LivingEntity entity, float tickDelta) {
        if (renderer instanceof PlayerEntityRenderer && entity instanceof AbstractClientPlayerEntity player)
            ((RendererInvokers.Player) renderer).runemodels$setModelPose(player);
        RendererInvokers.Living invoker = (RendererInvokers.Living) renderer;
        model.handSwingProgress = invoker.runemodels$getHandSwingProgress(entity, tickDelta);
        model.riding = entity.hasVehicle();
        model.child = entity.isBaby();

        float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
        float headYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw);
        float netHeadYaw = headYaw - bodyYaw;
        if (entity.hasVehicle() && entity.getVehicle() instanceof LivingEntity vehicle) {
            bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, vehicle.prevBodyYaw, vehicle.bodyYaw);
            float turn = MathHelper.clamp(MathHelper.wrapDegrees(headYaw - bodyYaw), -85, 85);
            bodyYaw = headYaw - turn;
            if (turn*turn > 2500) bodyYaw += turn*0.2f;
            netHeadYaw = headYaw - bodyYaw;
        }
        float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(entity)) {
            pitch *= -1;
            netHeadYaw *= -1;
        }

        float limbSpeed = 0, limbPos = 0;
        if (!entity.hasVehicle() && entity.isAlive()) {
            limbSpeed = Math.min(entity.limbAnimator.getSpeed(tickDelta), 1);
            limbPos = entity.limbAnimator.getPos(tickDelta);
            if (entity.isBaby()) limbPos *= 3;
        }
        model.animateModel(entity, limbPos, limbSpeed, tickDelta);
        model.setAngles(entity, limbPos, limbSpeed, invoker.runemodels$getAnimationProgress(entity, tickDelta), netHeadYaw, pitch);
    }

    /** All parts of a vanilla model, including nested ones */
    private static List<ModelPart> parts(EntityModel<?> model) {
        List<ModelPart> parts = new ArrayList<>();
        if (model instanceof SinglePartEntityModel<?> single) {
            single.getPart().traverse().forEach(parts::add);
        } else if (model instanceof AnimalModel<?>) {
            RendererInvokers.Animal animal = (RendererInvokers.Animal) model;
            for (ModelPart part: animal.runemodels$getHeadParts()) part.traverse().forEach(parts::add);
            for (ModelPart part: animal.runemodels$getBodyParts()) part.traverse().forEach(parts::add);
        }
        return parts;
    }

    /**
     * Copies how far the part has moved from its default pose. Vanilla axes have X and Y flipped compared to GeckoLib,
     * so pitch, yaw and the Y offset change sign (the same rule as scale(-1, -1, 1) elsewhere)
     */
    private static void copy(ModelPart part, GeoBone bone) {
        ModelTransform base = part.getDefaultTransform();
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.setRotX((rest == null?0:rest.getRotX()) - (part.pitch - base.pitch));
        bone.setRotY((rest == null?0:rest.getRotY()) - (part.yaw - base.yaw));
        bone.setRotZ((rest == null?0:rest.getRotZ()) + (part.roll - base.roll));
        bone.setPosX((rest == null?0:rest.getOffsetX()) + (part.pivotX - base.pivotX));
        bone.setPosY((rest == null?0:rest.getOffsetY()) - (part.pivotY - base.pivotY));
        bone.setPosZ((rest == null?0:rest.getOffsetZ()) + (part.pivotZ - base.pivotZ));
    }
}
