package com.kompsos.system.core.systemmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;

public class StateSystem extends Module {
    protected int tick = 0;

    public StateSystem() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("state_system"));
    }


    @Override
    public void onTick() {
        tick++;

        KlientConfig.StateManager stateManagerConfig = Klient.configLoader.getConfigObject().stateManager;
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer clientPlayer = instance.player;

        if (clientPlayer != null && stateManagerConfig.isCreativeState() && clientPlayer.gameMode() != GameType.CREATIVE) {
            if (instance.canSwitchGameMode()) {
                clientPlayer.connection.send(new ServerboundChangeGameModePacket(GameType.CREATIVE));
            }
        }

        if (clientPlayer != null && stateManagerConfig.isFlightState()) {
            clientPlayer.getAbilities().flying = true;
        }

        ClientSuggestionProvider providers = instance.getConnection().getSuggestionsProvider();

        if (clientPlayer != null && stateManagerConfig.isOperatorState() && !Commands.LEVEL_OWNERS.check(providers.permissions())) {
            if (instance.canSwitchGameMode() && tick % 5 == 0) {
                clientPlayer.connection.sendCommand("minecraft:op @s[type=player]");
            }
        }

        if (clientPlayer != null && !clientPlayer.getActiveEffects().isEmpty() && stateManagerConfig.isPotionState()) {
            clientPlayer.connection.sendCommand("minecraft:effect clear @s");
        }
    }
}
