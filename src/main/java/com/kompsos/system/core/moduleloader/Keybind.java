package com.kompsos.system.core.moduleloader;

import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

public class Keybind extends Module {
    public Keybind(ModuleManager.ExecutorMode mode, Identifier id) {
        super(mode, id);
    }
}
