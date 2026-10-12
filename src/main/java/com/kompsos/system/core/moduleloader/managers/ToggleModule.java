package com.kompsos.system.core.moduleloader.managers;

import com.kompsos.system.core.moduleloader.Module;
import net.minecraft.resources.Identifier;

public class ToggleModule extends Module {
    public boolean enabled = false;

    public ToggleModule(ModuleManager.ExecutorMode mode, Identifier id) {
        super(mode, id);
    }

    public void toggle() {
        enabled = !enabled;
    }
}
