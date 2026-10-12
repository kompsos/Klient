package com.kompsos.system.core.testmodules;

import com.ibm.icu.text.Normalizer2;
import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.resources.Identifier;

public class ChatCommand extends Command {
    public ChatCommand() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("chat"));

        setDescription(
                """
                Redundant test command:
                    I've run out of ideas because I'm not creative       \s
               \s"""
        );
    }
}
