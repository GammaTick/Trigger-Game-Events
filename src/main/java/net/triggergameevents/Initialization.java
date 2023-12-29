package net.triggergameevents;

import net.fabricmc.api.ModInitializer;

import net.triggergameevents.commands.TriggerGameEventCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initialization implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("game-events-trigger");

	@Override
	public void onInitialize() {
		TriggerGameEventCommand.register();
		LOGGER.info("Game Events Trigger has successfully loaded!");
	}
}