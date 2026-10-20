package rw.modden;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.api.RuneModelsEntrypoint;
import rw.modden.api.Paths;

public class RuneModels implements ModInitializer {
	public static final String MOD_ID = "runemodels";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		FabricLoader.getInstance()
				.getEntrypoints(MOD_ID, RuneModelsEntrypoint.class)
				.forEach(RuneModelsEntrypoint::onRuneModelsInit);

		ProfileSync.init();
		Commands.register();
		LOGGER.info("RuneModels ready, root: {}", Paths.getRoot());
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}