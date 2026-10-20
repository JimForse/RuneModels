package rw.modden.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import rw.modden.api.PlayMode;
import rw.modden.client.api.ClientApi;
import rw.modden.client.render.ClientProfiles;
import rw.modden.ProfileSync;
import rw.modden.profile.Profile;

public final class ClientNetwork {
    private ClientNetwork() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ProfileSync.PROFILE, (client, handler, buf, sender) -> {
            int id = buf.readVarInt();
            String model = buf.readString(), texture = buf.readString(), set = buf.readString();
            client.execute(() -> {
                if (model.isEmpty() && texture.isEmpty()) ClientProfiles.clear(id);
                else ClientProfiles.set(id, new Profile(blankToNull(model), blankToNull(texture), blankToNull(set)));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ProfileSync.PLAY, (client, handler, buf, sender) -> {
            int id = buf.readVarInt();
            String animation = buf.readString();
            int mode = buf.readVarInt();
            client.execute(() -> {
                Entity entity = client.world == null?null:client.world.getEntityById(id);
                if (entity == null) return;
                if (mode < 0 || animation.isEmpty()) { ClientApi.stopAnimation(entity); return; }
                PlayMode[] modes = PlayMode.values();
                if (mode < modes.length) ClientApi.playAnimation(entity, animation, modes[mode]);
            });
        });
    }

    private static String blankToNull(String s) { return s.isEmpty()?null:s; }
}