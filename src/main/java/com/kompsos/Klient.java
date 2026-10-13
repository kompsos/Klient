package com.kompsos;

import com.kompsos.system.core.systemmodules.ChatSystem;
import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.moduleloader.managers.CommandManager;
import com.kompsos.system.core.moduleloader.managers.KeybindManager;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.core.systemmodules.CommandBlockMatrixSystem;
import com.kompsos.system.core.systemmodules.NotificationSystem;
import com.kompsos.system.core.systemmodules.StateSystem;
import com.kompsos.system.features.commands.CoreTest;
import com.kompsos.system.features.commands.ExampleCommand;
import com.kompsos.system.features.commands.SingleRun;
import com.kompsos.system.features.keybinds.MainMenu;
import com.kompsos.system.features.keybinds.OpenChatCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Klient implements ModInitializer {
	public static final String MOD_ID = "klient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ConfigLoader configLoader = new ConfigLoader(
			FabricLoader.getInstance()
			.getConfigDir()
			.resolve(MOD_ID)
			.resolve("config.json"));

	//Systems
	public static final ChatSystem CHAT_SYSTEM = new ChatSystem();
	public static final CommandBlockMatrixSystem COMMAND_BLOCK_MATRIX_SYSTEM = new CommandBlockMatrixSystem();
	public static final NotificationSystem NOTIFICATION_SYSTEM = new NotificationSystem();
	public static final StateSystem STATE_SYSTEM = new StateSystem();

	//Managers
	public static CommandManager commandManager = (CommandManager) new CommandManager(new ExampleCommand(), new SingleRun(), new CoreTest()).load();

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Kaboom!");
		configLoader.load();

		new ModuleManager(CHAT_SYSTEM, NOTIFICATION_SYSTEM, STATE_SYSTEM, COMMAND_BLOCK_MATRIX_SYSTEM).load();
		new KeybindManager(new OpenChatCommand(), new MainMenu()).load();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
