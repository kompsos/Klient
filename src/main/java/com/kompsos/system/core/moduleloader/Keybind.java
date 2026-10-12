package com.kompsos.system.core.moduleloader;

import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.sdl.SDLKeycode;

public class Keybind extends Module {
    private int key = SDLKeycode.SDLK_UNKNOWN;
    public Keybind(ModuleManager.ExecutorMode mode, Identifier id) {
        super(mode, id);
    }
    public Keybind(ModuleManager.ExecutorMode mode, Identifier id, int key) {
        super(mode, id);
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
