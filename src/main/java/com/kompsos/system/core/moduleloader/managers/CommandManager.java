package com.kompsos.system.core.moduleloader.managers;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.Module;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandManager extends ModuleManager {
    public CommandManager(Command... commands) {super(commands);}

    public void processChatCommand(String input, CallbackInfo cancel) {
        String substringed = input.substring(1);
        ArrayList<String> collection = new ArrayList<>(Arrays.asList(substringed.split(" ")));

        String command = collection.removeFirst();

        for (Command module : (Command[]) getModules()) {
            if(module.getId().getPath().equals(command)) {
                Minecraft.getInstance().gui.hud.getChat().addRecentChat(input);
                module.execute(collection);
                cancel.cancel();
                return;
            }
        }
    }

    public Command getCommand(String name) {
        for (Module module : getModules()) {
            if (module.getId().getPath().equals(name))
                return (Command) module;
        }

        return new Command(ExecutorMode.MULTI, Klient.id("null"));
    }
}
