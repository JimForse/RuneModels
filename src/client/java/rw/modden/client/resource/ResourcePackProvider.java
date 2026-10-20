package rw.modden.client.resource;

import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import rw.modden.RuneModels;
import rw.modden.api.Paths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ResourcePackProvider implements net.minecraft.resource.ResourcePackProvider {
    public static final String PACK_ID = "runemodels_external";

    @Override
    public void register(Consumer<ResourcePackProfile> consumer) {
        Paths.prepareIfDefault();
        Path root = Paths.getRoot();

        if (!Files.isDirectory(root)) {
            RuneModels.LOGGER.warn("RuneModels: folder not found, nothing to mount: {}", root);
            return;
        }

        ResourcePackProfile profile = ResourcePackProfile.create(
                PACK_ID,
                Text.literal("RuneModels external assets"),
                true,
                name -> new DirectoryResourcePack(name, root, true),
                ResourceType.CLIENT_RESOURCES,
                ResourcePackProfile.InsertionPosition.TOP,
                ResourcePackSource.BUILTIN
        );

        if (profile == null) {
            RuneModels.LOGGER.warn("RuneModels: no valid pack.mcmeta in {}", root);
            return;
        }

        consumer.accept(profile);
        RuneModels.LOGGER.info("RuneModels: mounted resource folder {}", root);
    }
}