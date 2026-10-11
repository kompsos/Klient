package com.kompsos.mixin;

import com.kompsos.Klient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(at = @At("HEAD"), method = "handleSystemChat", cancellable = true)
    public void handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Klient.LOGGER.info(packet.content().getString());
    }
}
