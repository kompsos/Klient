package com.kompsos.system.core.moduleloader.managers;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleManager {
    private final com.kompsos.system.core.moduleloader.Module[] modules;
    private final ExecutorService main = Executors.newSingleThreadExecutor();
    private final ExecutorService multi = Executors.newCachedThreadPool();

    private boolean worldLoaded;

    public ModuleManager(com.kompsos.system.core.moduleloader.Module... modules) {
        this.modules = modules;
    }

    public void load() {
        ClientLifecycleEvents.CLIENT_STARTED.register(_ -> {
            for (com.kompsos.system.core.moduleloader.Module module : modules) {
                execute(module, module::clientReady);
                Klient.LOGGER.info(
                        "{} loaded as {}",
                        module.getId().getPath(),
                        module.getExecutorMode()
                );
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> {
            closeWorldIfLoaded();

            for (com.kompsos.system.core.moduleloader.Module module : modules) {
                execute(module, module::clientClosing);
            }

            multi.shutdown();
            main.shutdown();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
            closeWorldIfLoaded();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean hasWorld = client.player != null;

            if (hasWorld && !worldLoaded) {
                worldLoaded = true;

                for (com.kompsos.system.core.moduleloader.Module module : modules) {
                    execute(module, module::worldOpen);
                }
            } else if (!hasWorld && worldLoaded) {
                closeWorldIfLoaded();
            }

            if (hasWorld) {
                for (com.kompsos.system.core.moduleloader.Module module : modules) {
                    execute(module, module::onTick);
                }
            }

            tick();
        });
    }

    protected void tick() {}

    public Module[] getModules() {
        return modules;
    }

    private void closeWorldIfLoaded() {
        if (!worldLoaded) {
            return;
        }

        worldLoaded = false;

        for (com.kompsos.system.core.moduleloader.Module module : modules) {
            execute(module, module::worldClose);
        }
    }

    private void execute(Module module, Runnable task) {
        switch (module.getExecutorMode()) {
            case MULTI -> multi.execute(task);
            case SINGLE_MAIN -> task.run();
        }
    }

    public enum ExecutorMode {
        MULTI,
        SINGLE_MAIN
    }
}