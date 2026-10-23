package rw.modden;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import rw.modden.api.PlayMode;
import rw.modden.api.RuneModelsApi;
import rw.modden.profile.AnimationSets;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import rw.modden.profile.ProfileTags;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.EntityArgumentType.entities;
import static net.minecraft.command.argument.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class Commands {
    private static final SimpleCommandExceptionType BAD_NAME = new SimpleCommandExceptionType(
            Text.literal("Invalid name: use a-z, 0-9, _ (write other namespaces in quotes: \"mymod:name\")"));
    private static final SimpleCommandExceptionType RESERVED = new SimpleCommandExceptionType(
            Text.literal("Names starting with \"model.\", \"texture.\" or \"set.\" are reserved"));
    private static final SimpleCommandExceptionType NOTHING = new SimpleCommandExceptionType(
            Text.literal("Nothing changed (no targets, or the entity has no model for this action)"));
    /** Computed on the client from its own animation_sets.json, so it also works on a dedicated server */
    private static final SuggestionProvider<ServerCommandSource> ANIMATION_SETS = SuggestionProviders.register(
            RuneModels.id("animation_sets"), (c, builder) -> CommandSource.suggestMatching(AnimationSets.names(), builder));

    private Commands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
                literal("runemodels").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("set").then(argument("targets", entities())
                                .then(literal("model").then(argument("model", string())
                                        .executes(c -> model(c, false))
                                        .then(argument("texture", string()).executes(c -> model(c, true)))))
                                .then(literal("texture").then(argument("texture", string()).executes(Commands::texture)))
                                .then(argument("name", string()).executes(Commands::name))))
                        .then(literal("animlist")
                                .then(literal("set").then(argument("targets", entities())
                                        .then(argument("set", word()).suggests(ANIMATION_SETS)
                                                .executes(c -> animations(c, getString(c, "set"))))))
                                .then(literal("remove").then(argument("targets", entities())
                                        .executes(c -> animations(c, Profile.NO_ANIMATIONS)))))
                        .then(literal("clear").then(argument("targets", entities()).executes(Commands::clear)))
                        .then(literal("play").then(argument("targets", entities())
                                .then(argument("animation", string())
                                        .executes(c -> play(c, PlayMode.FILE))
                                        .then(literal("loop").executes(c -> play(c, PlayMode.LOOP)))
                                        .then(literal("once").executes(c -> play(c, PlayMode.ONCE)))
                                        .then(literal("hold").executes(c -> play(c, PlayMode.HOLD))))))
                        .then(literal("stop").then(argument("targets", entities()).executes(Commands::stop)))));
    }

    private static int name(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        String name = getString(c, "name");
        if (Names.parse(name) == null) throw BAD_NAME.create();
        if (ProfileTags.isReserved(name)) throw RESERVED.create();
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.apply(e, name)) n++;
        return done(c, n, "Profile applied to");
    }

    private static int model(CommandContext<ServerCommandSource> c, boolean withTexture) throws CommandSyntaxException {
        String model = getString(c, "model");
        String texture = withTexture?getString(c, "texture"):null;
        if (Names.parse(model) == null || withTexture && Names.parse(texture) == null) throw BAD_NAME.create();
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.setModel(e, model, texture)) n++;
        return done(c, n, "Model applied to");
    }

    private static int texture(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        String texture = getString(c, "texture");
        if (Names.parse(texture) == null) throw BAD_NAME.create();
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.setTexture(e, texture)) n++;
        return done(c, n, "Texture applied to");
    }

    private static int animations(CommandContext<ServerCommandSource> c, String set) throws CommandSyntaxException {
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.setAnimationSet(e, set)) n++;
        return done(c, n, Profile.NO_ANIMATIONS.equals(set)?"Custom animations removed from":"Animation set applied to");
    }

    private static int clear(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.clear(e)) n++;
        return done(c, n, "Cleared");
    }

    private static int play(CommandContext<ServerCommandSource> c, PlayMode mode) throws CommandSyntaxException {
        String animation = getString(c, "animation");
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.play(e, animation, mode)) n++;
        return done(c, n, "Animation started on");
    }

    private static int stop(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.stop(e)) n++;
        return done(c, n, "Animation stopped on");
    }

    private static int done(CommandContext<ServerCommandSource> c, int count, String message) throws CommandSyntaxException {
        if (count == 0) throw NOTHING.create();
        c.getSource().sendFeedback(() -> Text.literal(message+" "+count+" entit"+(count == 1?"y":"ies")), true);
        return count;
    }
}