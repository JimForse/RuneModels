package rw.modden.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.core.state.BoneSnapshot;

import java.util.List;

/** Draws the arm bone of the profile's geo model in place of the vanilla first-person arm */
public final class FirstPersonArm {
    private FirstPersonArm() {}

    /**
     * Called right before the vanilla arm is drawn, when the game has already posed it.
     * Returns true if the bone was drawn and the vanilla arm must not be drawn.
     * If the model has no such bone, the vanilla arm is drawn with the player's skin
     */
    public static boolean render(AbstractClientPlayerEntity player, ModelPart arm, boolean right,
                                 MatrixStack matrices, VertexConsumerProvider buffers, int light) {
        Profile profile = ClientProfiles.getUsable(player.getId());
        if (profile == null || profile.model() == null) return false;
        EntityRenderer renderer = RendererHolder.get();
        BakedGeoModel model = GeckoLibCache.getBakedModels().get(Names.model(profile.model()));
        if (renderer == null || model == null) return false;

        GeoBone bone = find(model.topLevelBones(), right?"rightarm":"leftarm");
        if (bone == null) return false;

        Identifier texture = Names.texture(profile.texture());
        RenderLayer layer = renderer.getGeoModel().getRenderType(renderer.getAnimatable(), texture);
        VertexConsumer buffer = buffers.getBuffer(layer);

        matrices.push();
        arm.rotate(matrices);
        matrices.scale(-1, -1, 1);
        matrices.translate(-bone.getPivotX()/16, -bone.getPivotY()/16, -bone.getPivotZ()/16);
        renderAtRest(renderer, bone, matrices, buffer, light);
        matrices.pop();
        return true;
    }

    private static GeoBone find(List<GeoBone> bones, String name) {
        for (GeoBone bone: bones) {
            if (bone.getName().replace("_", "").equalsIgnoreCase(name)) return bone;
            GeoBone found = find(bone.getChildBones(), name);
            if (found != null) return found;
        }
        return null;
    }

    /** The bone and its children in the pose from the model file, ignoring the current animation */
    private static void renderAtRest(EntityRenderer renderer, GeoBone bone, MatrixStack matrices, VertexConsumer buffer, int light) {
        BoneSnapshot rest = bone.getInitialSnapshot();
        float rotX = rest == null?bone.getRotX():rest.getRotX();
        float rotY = rest == null?bone.getRotY():rest.getRotY();
        float rotZ = rest == null?bone.getRotZ():rest.getRotZ();

        matrices.push();
        matrices.translate(bone.getPivotX()/16, bone.getPivotY()/16, bone.getPivotZ()/16);
        if (rotZ != 0) matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rotZ));
        if (rotY != 0) matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotY));
        if (rotX != 0) matrices.multiply(RotationAxis.POSITIVE_X.rotation(rotX));
        matrices.translate(-bone.getPivotX()/16, -bone.getPivotY()/16, -bone.getPivotZ()/16);

        for (GeoCube cube: bone.getCubes()) {
            matrices.push();
            renderer.renderCube(matrices, cube, buffer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
            matrices.pop();
        }
        for (GeoBone child: bone.getChildBones()) renderAtRest(renderer, child, matrices, buffer, light);
        matrices.pop();
    }
}
