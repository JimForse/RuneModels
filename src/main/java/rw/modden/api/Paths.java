package rw.modden.api;

import net.fabricmc.loader.api.FabricLoader;
import rw.modden.RuneModels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class Paths {
    /** Default root: <game folder>/runemodels */
    public static final Path DEFAULT_ROOT = FabricLoader.getInstance()
            .getGameDir().resolve(RuneModels.MOD_ID).toAbsolutePath().normalize();

    /** Current root. Marked as `volatile` because it may be read by different threads. */
    private static volatile Path root = DEFAULT_ROOT;

    private Paths() {}

    public static Path getRoot() {
        return root;
    }

    /** Setter: other mods can specify their own folder instead of the default one */
    public static void setRoot(Path newRoot) {
        Objects.requireNonNull(newRoot, "newRoot");
        root = newRoot.toAbsolutePath().normalize();
    }

    /** Restore the default path */
    public static void resetRoot() {
        root = DEFAULT_ROOT;
    }

    /** true if the path is standard—that is, if it is managed by RuneModels itself */
    public static boolean isDefault() {
        return root.equals(DEFAULT_ROOT);
    }

    /**
     * Creates the standard folders and `pack.mcmeta`, but ONLY if the path has not been changed
     * If the path is custom, we leave it alone; that is the responsibility of whoever defined it
     */
    public static void prepareIfDefault() {
        if (!isDefault()) {
            if (!Files.isDirectory(root)) {
                RuneModels.LOGGER.warn("Custom RuneModels path does not exist: {}", root);
            } else {
                RuneModels.LOGGER.info("Custom RuneModels path in use, skipping defaults: {}", root);
            }
            return;
        }

        try {
            Path ns = root.resolve("assets").resolve(RuneModels.MOD_ID);
            Files.createDirectories(ns.resolve("geo"));
            Files.createDirectories(ns.resolve("animations"));
            Files.createDirectories(ns.resolve("textures").resolve("entity"));

            Path mcmeta = root.resolve("pack.mcmeta");
            if (!Files.exists(mcmeta)) {
                Files.writeString(mcmeta,
                        "{\"pack\":{\"pack_format\":15,\"description\":\"RuneModels external assets\"}}");
            }
        } catch (IOException e) {
            RuneModels.LOGGER.error("Failed to create RuneModels folders in {}", root, e);
        }
    }
}