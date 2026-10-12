package com.kompsos.system.features.keybinds;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;

public class OpenChatCommand extends Keybind {
    public OpenChatCommand() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("open_command"), InputConstants.KEY_PERIOD);
    }

    @Override
    public void execute() {
        Minecraft.getInstance().setScreenAndShow(new ChatScreen(new KlientConfig.Commands().getPrefix(), false));
    }
}
