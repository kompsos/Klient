package com.kompsos.system.features.commands;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;

import java.util.ArrayList;

public class CoreTest extends Command {
    public CoreTest() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("core_test"));
    }

    @Override
    public void execute(ArrayList<String> arguments) {
        for (int i = 0; i < 100; i++) {
            infoLog("test");
            Klient.COMMAND_BLOCK_MATRIX_SYSTEM.run("say testing " + i);
        }
    }
}
