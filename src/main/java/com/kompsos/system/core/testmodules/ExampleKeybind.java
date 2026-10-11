package com.kompsos.system.core.testmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;

public class ExampleKeybind extends Keybind {
    public ExampleKeybind() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("examplek"));
    }

    @Override
    public void execute() {
        infoLog("keybind pressed");
    }
}
