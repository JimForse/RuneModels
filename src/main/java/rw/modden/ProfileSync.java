package rw.modden;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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
    }

    private static void syncSelf(ServerPlayerEntity player) {
        Profile profile = ProfileTags.read(player);
        if (profile != null) sendProfile(player, player, profile);
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
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(entity.getId());
            buf.writeString(animation == null?"":animation);
            buf.writeVarInt(mode == null?-1:mode.ordinal());
            ServerPlayNetworking.send(player, PLAY, buf);
        }
    }

    /** profile == null means "remove the profile" */
    private static void sendProfile(ServerPlayerEntity player, Entity entity, Profile profile) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entity.getId());
        buf.writeString(profile == null || profile.model() == null?"":profile.model());
        buf.writeString(profile == null || profile.texture() == null?"":profile.texture());
        buf.writeString(profile == null || profile.animations() == null?"":profile.animations());
        ServerPlayNetworking.send(player, PROFILE, buf);
    }

    private static List<ServerPlayerEntity> audience(Entity entity) {
        List<ServerPlayerEntity> list = new ArrayList<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayerEntity self && !list.contains(self)) list.add(self);
        return list;
    }
}