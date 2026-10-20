package rw.modden;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import rw.modden.api.PlayMode;
import rw.modden.api.RuneModelsApi;
import rw.modden.profile.Names;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.entities;
import static net.minecraft.command.argument.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class Commands {
    private static final SimpleCommandExceptionType BAD_NAME = new SimpleCommandExceptionType(
            Text.literal("Invalid name: use a-z, 0-9, _ (write other namespaces in quotes: \"mymod:name\")"));
    private static final SimpleCommandExceptionType NOTHING = new SimpleCommandExceptionType(
            Text.literal("Nothing changed (no targets, or the entity has no model for this action)"));

    private Commands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
                literal("runemodels").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("set").then(argument("targets", entities())
                                .then(argument("model", string()).then(argument("texture", string())
                                        .executes(c -> set(c, null))
                                        .then(argument("animations", string())
                                                .executes(c -> set(c, getString(c, "animations"))))))))
                        .then(literal("texture").then(argument("targets", entities())
                                .then(argument("texture", string()).executes(Commands::texture))))
                        .then(literal("clear").then(argument("targets", entities()).executes(Commands::clear)))
                        .then(literal("play").then(argument("targets", entities())
                                .then(argument("animation", string())
                                        .executes(c -> play(c, PlayMode.FILE))
                                        .then(literal("loop").executes(c -> play(c, PlayMode.LOOP)))
                                        .then(literal("once").executes(c -> play(c, PlayMode.ONCE)))
                                        .then(literal("hold").executes(c -> play(c, PlayMode.HOLD))))))
                        .then(literal("stop").then(argument("targets", entities()).executes(Commands::stop)))));
    }

    private static int set(CommandContext<ServerCommandSource> c, String set) throws CommandSyntaxException {
        String model = getString(c, "model"), texture = getString(c, "texture");
        if (Names.parse(model) == null || Names.parse(texture) == null) throw BAD_NAME.create();
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.setModel(e, model, texture, set)) n++;
        return done(c, n, "Model applied to");
    }

    private static int texture(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        String texture = getString(c, "texture");
        if (Names.parse(texture) == null) throw BAD_NAME.create();
        int n = 0;
        for (Entity e: getEntities(c, "targets")) if (RuneModelsApi.setTexture(e, texture)) n++;
        return done(c, n, "Texture applied to");
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