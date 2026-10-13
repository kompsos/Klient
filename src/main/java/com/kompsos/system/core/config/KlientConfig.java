package com.kompsos.system.core.config;

import com.google.gson.annotations.SerializedName;

public class KlientConfig {

    @SerializedName("Commands")
    public Commands commands = new Commands();

    @SerializedName("Miscellaneous")
    public Miscellaneous miscellaneous = new Miscellaneous();

    @SerializedName("StateManager")
    public StateManager stateManager = new StateManager();

    @SerializedName("CommandBlockMatrix")
    public CommandBlockMatrix commandBlockMatrix = new CommandBlockMatrix();

    public static class Commands {
        boolean enabled = true;
        int maxAutoCmds = 5;
        String commandPrefix = ".";

        public boolean getCommandsEnabled() {return enabled;}
        public String getPrefix() {return commandPrefix;}
        public int getMaxAutoCmds() {return maxAutoCmds;}
    }

    public static class StateManager {
        boolean creativeState = false;
        boolean operatorState = false;
        boolean potionState = false;
        boolean flightState = false;
        boolean attemptCMDBlock = false;

        public boolean isAttemptCMDBlock() {return attemptCMDBlock;}
        public boolean isFlightState() {return flightState;}
        public boolean isPotionState() {return potionState;}
        public boolean isCreativeState() {return creativeState;}
        public boolean isOperatorState() {return operatorState;}
    }

    public static class CommandBlockMatrix {
        boolean enabled = false;
        boolean onlyCertifiedServers = true;
        boolean chatFill = true;
        int zHeight = 3;
        int area = 16;
        int maxQueue = 2048;

        public boolean isEnabled() {return enabled;}
        public boolean isOnlyCertifiedServers() {return onlyCertifiedServers;}
        public boolean isChatFill() {return chatFill;}
        public int getzHeight() {return zHeight;}
        public int getArea() {return area;}
        public int getMaxQueue() {return maxQueue;}
    }

    public static class Miscellaneous {
        String[] officalKaboomServers = new String[] {"kaboom.pw","kaboom.chipmunk.land","kaboom.chayapak1.dev","kbm.optmstc.dev","kaboom.hcesaropz.dev", "kaboom.clickbyte.eu", "norat.pw"};
        public String[] getOfficalKaboomServers() {return officalKaboomServers;}
    }
}
