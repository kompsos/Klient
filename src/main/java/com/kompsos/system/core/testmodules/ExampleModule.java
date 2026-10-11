package com.kompsos.system.core.testmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.ModuleManager;
import net.minecraft.resources.Identifier;

public class ExampleModule extends Module {
    public ExampleModule() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("example_module"));
    }

    @Override
    public void onTick() {
        infoLog("Ticking");
    }

    @Override
    public void worldClose() {
        infoLog("World closed");
    }

    @Override
    public void worldOpen() {
        infoLog("World opened");
    }

    @Override
    public void clientReady() {
        infoLog("Client opened");
    }

    @Override
    public void clientClosing() {
        infoLog("Client closing");
    }
}
