package com.kompsos.system.features.guis.mainscreen;

import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.gui.Entry;
import com.kompsos.system.core.gui.MenuScreen;
import com.kompsos.system.core.gui.Panel;

public class MainScreen extends MenuScreen {
    public MainScreen(ConfigLoader loader) {
        Panel settings = new Panel("Settings").add(new Entry.Slider("Speed", 0, 5, 1, v -> {
        }));
        Panel main = new Panel("Modules").add(new Entry.PanelToggle("Settings", settings));

        Panel[] panels = new Panel[]{
                main,
                ConfigPanelBuilder.build(loader, "Commands"),
                ConfigPanelBuilder.build(loader, "StateManager"),
                ConfigPanelBuilder.build(loader, "CommandBlockMatrix")
        };

        super(panels);
    }
}