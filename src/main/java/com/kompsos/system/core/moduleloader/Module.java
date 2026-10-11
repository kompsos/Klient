package com.kompsos.system.core.moduleloader;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

public class Module {
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

    public KlientConfig config() {
        return Klient.getConfig();
    }
    public void execute() {}
    public void onTick() {}
    public void worldOpen() {}
    public void worldClose() {}
    public void clientReady() {}
    public void clientClosing() {}
}
