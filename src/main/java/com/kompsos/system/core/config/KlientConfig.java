package com.kompsos.system.core.config;

import com.google.gson.annotations.SerializedName;

public class KlientConfig {

    @SerializedName("Commands")
    public Commands commands = new Commands();

    public static class Commands {
        boolean enabled = true;
        int maxShowedAutocompleteCommands = 5;
        String commandPrefix = ".";

        public boolean getCommandsEnabled() {return enabled;}
        public String getPrefix() {return commandPrefix;}
        public int getMaxShowedAutocompleteCommands() {return maxShowedAutocompleteCommands;}
    }
}
