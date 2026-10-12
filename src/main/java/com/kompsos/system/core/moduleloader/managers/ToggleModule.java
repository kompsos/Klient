package com.kompsos.system.core.moduleloader.managers;

import com.kompsos.system.core.moduleloader.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class ToggleModule extends Module {
    public boolean enabled = false;

    public ToggleModule(ModuleManager.ExecutorMode mode, Identifier id) {
        super(mode, id);
    }

    public void toggle() {
        if(enabled) {
            enabled = false;
        } else {
            enabled = true;
        }
    }
}
