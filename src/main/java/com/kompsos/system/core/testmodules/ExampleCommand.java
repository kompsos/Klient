package com.kompsos.system.core.testmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;

import java.util.ArrayList;

public class ExampleCommand extends Command {
    public ExampleCommand() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("examplecommand"));

        setDescription(
                """
                This is a general description of this command
                    Hopefully you were not expecting something detailed lol.
                   \s
                    Yada Yada: examplecommand <argument> <argument>
               \s"""
        );
    }

    @Override
    public void execute(ArrayList<String> arguments) {
        infoLog("Test command ran");
        infoLog(arguments.toString());
    }
}
