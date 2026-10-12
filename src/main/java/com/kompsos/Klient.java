package com.kompsos;

import com.kompsos.system.core.systemmodules.ChatModule;
import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.moduleloader.managers.CommandManager;
import com.kompsos.system.core.moduleloader.managers.KeybindManager;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.core.testmodules.*;
import com.kompsos.system.features.keybinds.OpenChatCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Klient implements ModInitializer {
	public static final String MOD_ID = "klient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final ConfigLoader configLoader = new ConfigLoader(
			FabricLoader.getInstance()
			.getConfigDir()
			.resolve(MOD_ID)
			.resolve("config.json"));

	public static final ChatModule chatModule = new ChatModule();

	public static CommandManager commandManager = (CommandManager) new CommandManager(
			new ExampleCommand(),
			new ChatCommand(),
			new BlankCommand()
	).load();

	public static KeybindManager keybindManager = (KeybindManager) new KeybindManager(new ExampleKeybind(), new OpenChatCommand()).load();;
	public static ModuleManager moduleManager = new ModuleManager(chatModule, new ExampleModule()).load();

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Kaboom!");
		configLoader.load();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
