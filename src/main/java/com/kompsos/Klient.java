package com.kompsos;

import com.kompsos.system.core.moduleloader.ModuleManager;
import com.kompsos.system.core.testmodules.ExampleModule;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Klient implements ModInitializer {
	public static final String MOD_ID = "klient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModuleManager moduleManager;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Kaboom!");
		moduleManager = new ModuleManager(new ExampleModule());

		moduleManager.load();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
