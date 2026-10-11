package com.kompsos.system.core.moduleloader;

import com.kompsos.Klient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleManager {
    private final Module[] modules;
    private final ExecutorService main = Executors.newSingleThreadExecutor();
    private final ExecutorService multi = Executors.newCachedThreadPool();

    public ModuleManager(Module... modules) {
        this.modules = modules;
    }

    public void load() {
        AtomicBoolean worldLoaded = new AtomicBoolean(false);

        ClientLifecycleEvents.CLIENT_STARTED.register(_ -> moduleIterator().forEachRemaining(module -> {
            switch (module.getExecutorMode()) {
                case MULTI -> multi.execute(module::clientReady);
                case SINGLE_MAIN -> main.execute(module::clientReady);
            }

            Klient.LOGGER.info("{} loaded as {}", module.getId().getPath(), module.getExecutorMode());
        }));

        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> moduleIterator().forEachRemaining(module -> {
            switch (module.getExecutorMode()) {
                case MULTI -> multi.execute(module::clientClosing);
                case SINGLE_MAIN -> main.execute(module::clientClosing);
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> moduleIterator().forEachRemaining(module -> {
            if (worldLoaded.get()) {
                switch (module.getExecutorMode()) {
                    case MULTI -> multi.execute(module::worldClose);
                    case SINGLE_MAIN -> main.execute(module::worldClose);
                }

                worldLoaded.set(false);
            }
        }));

        ClientTickEvents.END_LEVEL_TICK.register(_ -> moduleIterator().forEachRemaining(module -> {
            switch (module.getExecutorMode()) {
                case MULTI -> multi.execute(module::onTick);
                case SINGLE_MAIN -> main.execute(module::onTick);
            }

            if (!worldLoaded.get()) {
                switch (module.getExecutorMode()) {
                    case MULTI -> multi.execute(module::worldOpen);
                    case SINGLE_MAIN -> main.execute(module::worldOpen);
                }

                worldLoaded.set(true);
            }
        }));
    }

    private Iterator<Module> moduleIterator() {
        return Arrays.stream(modules).iterator();
    }


    public enum ExecutorMode {
        MULTI,
        SINGLE_MAIN
    }
}
