package com.kompsos.system.core.testmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Keybind;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.network.chat.Component;

public class ExampleKeybind extends Keybind {
    public ExampleKeybind() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("examplek"));
    }

    @Override
    public void execute() {
        notify(Component.literal("test for the notification system so this should be a rather long text"), 20 * 4);
    }
}
