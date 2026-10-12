package com.kompsos.system.core.moduleloader;

import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;

public class Command extends Module {
    private String description = "No description provided.";

    public Command(ModuleManager.ExecutorMode mode, Identifier id) {
        super(mode, id);
    }

    @Override
    public void execute(ArrayList<String> arguments) {super.execute();}

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
