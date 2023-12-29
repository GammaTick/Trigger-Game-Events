package net.gameeventstrigger;

import net.fabricmc.api.ModInitializer;

import net.gameeventstrigger.commands.TriggerGameEventCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEventsTrigger implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("game-events-trigger");

	@Override
	public void onInitialize() {
		TriggerGameEventCommands.register();
		LOGGER.info("Game Events Trigger has successfully loaded!");
	}
}