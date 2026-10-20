package rw.modden.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import rw.modden.RuneModels;
import rw.modden.api.Paths;
import rw.modden.client.render.ClientProfiles;

public class RuneModelsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Paths.prepareIfDefault();
		ClientNetwork.init();
		ListCommand.register();
		AnimationSetsLoader.register();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientProfiles.clearAll());
		RuneModels.LOGGER.info("RuneModels client init");
	}
}