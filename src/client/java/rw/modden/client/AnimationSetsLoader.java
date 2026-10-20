package rw.modden.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import rw.modden.RuneModels;
import rw.modden.api.Paths;
import rw.modden.client.render.ProfileValidator;
import rw.modden.profile.AnimationSets;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Reads <runemodels folder>/animation_sets.json at startup and whenever resources are reloaded (F3+T) */
public final class AnimationSetsLoader implements SimpleSynchronousResourceReloadListener {
    private static final Set<String> FROM_FILE = ConcurrentHashMap.newKeySet();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new AnimationSetsLoader());
    }
    @Override
    public Identifier getFabricId() {
        return new Identifier(RuneModels.MOD_ID, "animation_sets");
    }
    @Override
    public void reload(ResourceManager manager) {
        load();
        ProfileValidator.reset();
    }

    private static void load() {
        for (String set: FROM_FILE) AnimationSets.remove(set);
        FROM_FILE.clear();

        Path file = Paths.getRoot().resolve("animation_sets.json");
        if (!Files.isRegularFile(file)) return;

        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonElement sets = root.isJsonObject()?root.getAsJsonObject().get("animation_sets"):null;
            if (sets == null || !sets.isJsonObject()) {
                warn("expected an object 'animation_sets' at the top level");
                return;
            }
            for (Map.Entry<String, JsonElement> set: sets.getAsJsonObject().entrySet()) {
                readSet(set.getKey(), set.getValue());
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            warn("could not read file: " + e.getMessage());
        }
    }

    private static void readSet(String setName, JsonElement value) {
        if (!value.isJsonObject()) { warn("set '" + setName + "' must be an object, skipped"); return; }
        if (AnimationSets.create(setName)) FROM_FILE.add(setName);

        for (Map.Entry<String, JsonElement> situation: value.getAsJsonObject().entrySet()) {
            if (!situation.getValue().isJsonArray()) {
                warn("'" + setName + "." + situation.getKey() + "' must be a list of names, skipped");
                continue;
            }
            for (JsonElement name: situation.getValue().getAsJsonArray()) {
                if (name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()) {
                    AnimationSets.addAnimation(setName, situation.getKey(), name.getAsString());
                } else {
                    warn("'" + setName + "." + situation.getKey() + "' contains a non-text item, skipped");
                }
            }
        }
    }
    private static void warn(String message) {
        RuneModels.LOGGER.warn("RuneModels animation_sets.json: {}", message);
    }
}