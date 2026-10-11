package com.kompsos;

import com.kompsos.system.core.chatprocessor.ChatModule;
import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.KeybindManager;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.core.testmodules.ExampleKeybind;
import com.kompsos.system.core.testmodules.ExampleModule;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Klient implements ModInitializer {
	public static final String MOD_ID = "klient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static ConfigLoader configLoader = new ConfigLoader(
			FabricLoader.getInstance()
			.getConfigDir()
			.resolve(MOD_ID)
			.resolve("config.json"));

	private static Module[] coreModules = {new ChatModule()};
	private static Module[] exampleModules = {new ExampleModule()};

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Kaboom!");
		new ModuleManager(ArrayUtils.addAll(coreModules, exampleModules)).load();
		new KeybindManager(new ExampleKeybind()).load();

		configLoader.load();
	}

	public static KlientConfig getConfig() {
		return configLoader.getConfigObject();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
