package rw.modden;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.api.PlayMode;
import rw.modden.profile.ProfileTags;
import rw.modden.profile.Profile;
import java.util.ArrayList;
import java.util.List;

/** Sending profiles and playback commands to clients
 *  like a ServerNetworking, yeah */
public final class ProfileSync {
    public static final Identifier PROFILE = RuneModels.id("profile");
    public static final Identifier PLAY = RuneModels.id("play");
    private static boolean silent;

    private ProfileSync() {}

    public static void init() {
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            Profile profile = ProfileTags.read(entity);
            if (profile != null) sendProfile(player, entity, profile);
        });
        EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
            if (ProfileTags.read(entity) != null) sendProfile(player, entity, null);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncSelf(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> syncSelf(newPlayer));
        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(PROFILE)) server.execute(() -> syncAll(handler.player));
        });
    }

    private static void syncSelf(ServerPlayerEntity player) {
        Profile profile = ProfileTags.read(player);
        if (profile != null) sendProfile(player, player, profile);
    }

    /** Everything this player should know: their own profile and the profiles of all entities they already see */
    private static void syncAll(ServerPlayerEntity player) {
        syncSelf(player);
        for (Entity entity: player.getServerWorld().iterateEntities()) {
            if (entity == player || !PlayerLookup.tracking(entity).contains(player)) continue;
            Profile profile = ProfileTags.read(entity);
            if (profile != null) sendProfile(player, entity, profile);
        }
    }

    public static void silently(Runnable action) {
        boolean before = silent;
        silent = true;
        try { action.run(); } finally { silent = before; }
    }

    /** Called by a mixin when a "runemodels.*" tag is modified */
    public static void onTagsChanged(Entity entity) {
        if (silent || entity.getWorld().isClient()) return;
        syncProfile(entity);
    }

    public static void syncProfile(Entity entity) {
        Profile profile = ProfileTags.read(entity);
        for (ServerPlayerEntity player: audience(entity)) sendProfile(player, entity, profile);
    }

    /** mode == null means "stop" */
    public static void sendPlay(Entity entity, String animation, PlayMode mode) {
        for (ServerPlayerEntity player: audience(entity)) {
            if (!ServerPlayNetworking.canSend(player, PLAY)) continue;
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(entity.getId());
            buf.writeString(animation == null?"":animation);
            buf.writeVarInt(mode == null?-1:mode.ordinal());
            ServerPlayNetworking.send(player, PLAY, buf);
        }
    }

    /** profile == null means "remove the profile" */
    private static void sendProfile(ServerPlayerEntity player, Entity entity, Profile profile) {
        if (!ServerPlayNetworking.canSend(player, PROFILE)) return;
        Profile p = profile == null?Profile.EMPTY:profile;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entity.getId());
        buf.writeString(orEmpty(p.name()));
        buf.writeString(orEmpty(p.model()));
        buf.writeString(orEmpty(p.texture()));
        buf.writeString(orEmpty(p.animations()));
        ServerPlayNetworking.send(player, PROFILE, buf);
    }

    private static String orEmpty(String s) { return s == null?"":s; }

    private static List<ServerPlayerEntity> audience(Entity entity) {
        List<ServerPlayerEntity> list = new ArrayList<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayerEntity self && !list.contains(self)) list.add(self);
        return list;
    }
}