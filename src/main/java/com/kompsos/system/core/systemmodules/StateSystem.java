package com.kompsos.system.core.systemmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class StateSystem extends Module {
    protected int tick = 0;

    public StateSystem() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("state_system"));
    }

    public void onServerPosPacket(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        KlientConfig.Miscellaneous miscellaneousConfig = Klient.configLoader.getConfigObject().miscellaneous;
        Minecraft instance = Minecraft.getInstance();

        if (miscellaneousConfig.isPositionState() && instance.player != null) {
            ci.cancel();


            Vec3 pos = instance.player.position();
            Vec3 serverPos = packet.change().position();

            instance.getConnection().send(new ServerboundAcceptTeleportationPacket(packet.id(), serverPos.x, serverPos.y, serverPos.z, packet.change().xRot(), packet.change().yRot()));

            if (pos.distanceTo(serverPos) >= 128) {
                instance.getConnection().sendCommand(String.format("minecraft:tp %s %s %s %s", instance.player.getPlainTextName(), pos.x, pos.y, pos.z));
            } else {
                instance.player.setPos(pos);
            }

        }
    }

    @Override
    public void onTick() {
        tick++;

        KlientConfig.Miscellaneous miscellaneousConfig = Klient.configLoader.getConfigObject().miscellaneous;
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer clientPlayer = instance.player;

        if (clientPlayer != null && miscellaneousConfig.isCreativeState() && clientPlayer.gameMode() != GameType.CREATIVE) {
            if (instance.canSwitchGameMode()) {
                clientPlayer.connection.send(new ServerboundChangeGameModePacket(GameType.CREATIVE));
            }
        }

        if (clientPlayer != null && miscellaneousConfig.isFlightState()) {
            clientPlayer.getAbilities().flying = true;
        }

        ClientSuggestionProvider providers = instance.getConnection().getSuggestionsProvider();

        if (clientPlayer != null && miscellaneousConfig.isOperatorState() && !Commands.LEVEL_OWNERS.check(providers.permissions())) {
            if (instance.canSwitchGameMode() && tick % 5 == 0) {
                clientPlayer.connection.sendCommand("minecraft:op @s[type=player]");
            }
        }

        if (clientPlayer != null && !clientPlayer.getActiveEffects().isEmpty() && tick % 1 == 0 && miscellaneousConfig.isPotionState()) {
            clientPlayer.connection.sendCommand("minecraft:effect clear @s");
        }
    }
}
