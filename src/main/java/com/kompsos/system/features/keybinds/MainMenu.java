package com.kompsos.system.features.keybinds;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.features.guis.mainscreen.MainScreen;
import net.minecraft.client.Minecraft;

public class MainMenu extends Keybind {
    private MainScreen screen;
    public MainMenu() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("open_main_menu"));
    }

    @Override
    public void clientReady() {
        screen = new MainScreen(Klient.configLoader);
    }

    @Override
    public void execute() {
        Minecraft.getInstance().setScreenAndShow(screen);
    }
}
