package com.kompsos.system.features.commands;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.lib.ArrayTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;

public class SingleRun extends Command {
    public SingleRun() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("single_run"));
    }

    @Override
    public void execute(ArrayList<String> arguments) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Klient.COMMAND_BLOCK_MATRIX_SYSTEM.singleBlockRun(ArrayTools.join(arguments, ' '));
        }
    }
}
