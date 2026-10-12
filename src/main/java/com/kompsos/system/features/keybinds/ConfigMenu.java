package com.kompsos.system.features.keybinds;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.features.guis.ConfigScreen;
import net.minecraft.client.Minecraft;

public class ConfigMenu extends Keybind {
    public ConfigMenu() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("open_main_menu"));
    }

    @Override
    public void execute() {
        Minecraft.getInstance().setScreenAndShow(
                        new ConfigScreen(Klient.configLoader)
                );
    }
}
