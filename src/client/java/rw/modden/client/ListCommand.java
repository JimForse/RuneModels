package rw.modden.client;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rw.modden.client.api.ClientApi;
import software.bernie.geckolib.cache.GeckoLibCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/** /rmlist displays the loaded models,
 * while /rmlist <model> shows the animations for that model.
 * Client-side only: the files reside on the client. */
public final class ListCommand {
    private ListCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((d, r) -> d.register(literal("rmlist")
                .executes(ListCommand::models)
                .then(argument("model", string()).executes(ListCommand::animations))));
    }

    private static int models(CommandContext<FabricClientCommandSource> c) {
        List<String> names = new ArrayList<>();
        for (Identifier id: GeckoLibCache.getBakedModels().keySet()) {
            String path = id.getPath();
            if (path.startsWith("geo/") && path.endsWith(".geo.json"))
                names.add(id.getNamespace() + ":" + path.substring(4, path.length() - 9));
        }
        Collections.sort(names);
        c.getSource().sendFeedback(Text.literal(names.isEmpty()?"No models loaded":"Models: "+String.join(", ", names)));
        return names.size();
    }

    private static int animations(CommandContext<FabricClientCommandSource> c) {
        String[] names = ClientApi.listAnimations(getString(c, "model"));
        c.getSource().sendFeedback(Text.literal(names.length == 0
                ?"No animation file found for this model"
                :"Animations: " + String.join(", ", names)));
        return names.length;
    }
}