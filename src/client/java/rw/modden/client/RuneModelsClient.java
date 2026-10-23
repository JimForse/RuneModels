package rw.modden.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import rw.modden.RuneModels;
import rw.modden.api.Paths;
import rw.modden.client.render.ClientProfiles;
import rw.modden.client.render.RendererHolder;

public class RuneModelsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Paths.prepareIfDefault();
		ClientNetwork.init();
		ListCommand.register();
		AnimationSetsLoader.register();
		// Profiles stay: the player's own entity is unloaded and created again with the same id when changing dimension
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			ClientProfiles.clearPlay(entity.getId());
			RendererHolder.forget(entity.getId());
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientProfiles.clearAll();
			RendererHolder.forgetAll();
		});
		RuneModels.LOGGER.info("RuneModels client init");
	}
}
