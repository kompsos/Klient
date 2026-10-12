package com.kompsos.system.core.moduleloader;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;

public abstract class Module {
    private final ModuleManager.ExecutorMode mode;
    private final Identifier id;
    public Module(ModuleManager.ExecutorMode mode, Identifier id) {
        this.mode = mode;
        this.id = id;
    }

    public void infoLog(String data) {
        Klient.LOGGER.info("{} <-> {}", getId().getPath(), data);
    }

    public ModuleManager.ExecutorMode getExecutorMode() {
        return this.mode;
    }

    public Identifier getId() {
        return this.id;
    }

    public void execute() {}
    public void onTick() {}
    public void worldOpen() {}
    public void worldClose() {}
    public void clientReady() {}
    public void clientClosing() {}

    public void execute(ArrayList<String> arguments) {}
}
