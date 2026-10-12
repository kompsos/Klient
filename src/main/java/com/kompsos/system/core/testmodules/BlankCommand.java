package com.kompsos.system.core.testmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

public class BlankCommand extends Command {
    public BlankCommand() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("blank"));
    }
}
