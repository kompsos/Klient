package com.kompsos.system.core.config;

import com.google.gson.annotations.SerializedName;

public class KlientConfig {

    @SerializedName("Commands")
    public Commands commands = new Commands();

    @SerializedName("Miscellaneous")
    public Miscellaneous miscellaneous = new Miscellaneous();

    public static class Commands {
        boolean enabled = true;
        int maxAutoCmds = 5;
        String commandPrefix = ".";

        public boolean getCommandsEnabled() {return enabled;}
        public String getPrefix() {return commandPrefix;}
        public int getMaxAutoCmds() {return maxAutoCmds;}
    }

    public static class Miscellaneous {
        String[] officalKaboomServers = new String[] {"kaboom.pw","kaboom.chipmunk.land","kaboom.chayapak1.dev","kbm.optmstc.dev","kaboom.hcesaropz.dev", "kaboom.clickbyte.eu", "norat.pw"};
        boolean creativeState = false;
        boolean operatorState = false;
        boolean potionState = false;
        boolean flightState = false;

        public boolean isFlightState() {return flightState;}
        public boolean isPotionState() {return potionState;}
        public boolean isCreativeState() {return creativeState;}
        public boolean isOperatorState() {return operatorState;}
        public String[] getOfficalKaboomServers() {return officalKaboomServers;}
    }
}
