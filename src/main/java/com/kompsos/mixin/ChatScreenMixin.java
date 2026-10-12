package com.kompsos.mixin;

import com.kompsos.Klient;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "handleChatInput", cancellable = true)
    public void handleChatInput(String msg, boolean addToRecent, CallbackInfo ci) {
        Klient.CHAT_SYSTEM.handleClientsideChatMessage(msg, addToRecent, ci);
    }


}