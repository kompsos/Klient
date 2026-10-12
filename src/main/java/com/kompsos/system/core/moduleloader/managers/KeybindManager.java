package com.kompsos.system.core.moduleloader.managers;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Keybind;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;

public class KeybindManager extends ModuleManager {
    public HashMap<Keybind, KeyMapping> mappedKeybindings = new HashMap<>();
    public KeyMapping.Category category = KeyMapping.Category.register(
            Klient.id("keybinds")
    );

    public KeybindManager(Keybind... keybinds) {
        super(keybinds);
    }

    @Override
    public ModuleManager load() {
        super.load();

        for (Keybind module : (Keybind[]) getModules()) {
                mappedKeybindings.put(module, KeyMappingHelper.registerKeyMapping(new KeyMapping(
                                module.getId().getPath(),
                                InputConstants.Type.KEYBOARD,
                                InputConstants.UNKNOWN.getValue(), category
                        )
                ));
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        mappedKeybindings.forEach((keybind, mapping) -> {
            while (mapping.consumeClick()) {
                keybind.execute();
            }
        });
    }
}
