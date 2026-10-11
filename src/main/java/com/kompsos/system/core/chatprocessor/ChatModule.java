package com.kompsos.system.core.chatprocessor;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.ModuleManager;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ChatModule extends Module {
    public ChatModule() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("chat_module"));
    }

    public void handleClientsideChatMessage(String msg, boolean addToRecent, CallbackInfo ci) {
        //Handle logic for command system later
    }

    public void handleSystemMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        //Handle fill messages later
    }
}
