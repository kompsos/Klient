package com.kompsos.system.core.chatprocessor;

import com.kompsos.Klient;
import com.kompsos.mixin.ChatScreenAccessor;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.lib.ClientColors;
import com.kompsos.system.lib.GraphicsUtil;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2f;
import org.lwjgl.sdl.SDLKeycode;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatModule extends Module {
    public ChatModule() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("chat_module"));
    }

    @Override
    public void clientReady() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // If the screen is the active Chat Input screen, hook into its render pass
            if (screen instanceof ChatScreen) {
                ScreenEvents.afterExtract(screen).register((chatScreen, guiGraphicsExtractor, _, _, _) -> {
                    handleAutocomplete(client, scaledWidth, scaledHeight, guiGraphicsExtractor, chatScreen);
                });
            }
        });
    }

    public void handleClientsideChatMessage(String msg, boolean addToRecent, CallbackInfo ci) {
        KlientConfig.Commands commandConfig = new KlientConfig.Commands();

        if (commandConfig.getCommandsEnabled() && msg.startsWith(commandConfig.getPrefix())) {
            Klient.commandManager.processChatCommand(msg, ci);
        }
    }

    private int cursorIndex = 0;

    public void handleAutocomplete(Minecraft instance, int scaledWidth, int scaledHeight, GuiGraphicsExtractor drawContext, Screen screen) {
        int visibleCommands;
        int color = ARGB.color((int) (255 / 1.125),12,12,12);
        int elementX = 3;
        int elementY = scaledHeight - 15;
        int textPadding = 2;
        int width = 225;
        int height;

        KlientConfig.Commands commandConfig = new KlientConfig.Commands();
        Command[] commands = (Command[]) Klient.commandManager.getModules();
        ChatComponent chatScreen = instance.gui.hud.getChat();
        EditBox inputBox = ((ChatScreenAccessor) screen).getInput();

        if (chatScreen.isChatFocused() && inputBox.getValue().startsWith(commandConfig.getPrefix())) {
            int commandsAvailable = 0;
            ArrayList<Component> namedCommandsArray = new ArrayList<>();
            for (Command command : commands) {
                String commandName = command.getId().getPath();
                if (commandName.contains(inputBox.getValue().substring(1))) {
                    commandsAvailable++;
                    namedCommandsArray.add(Component.literal(commandName));
                }
            }

            visibleCommands = Math.clamp(commandsAvailable, 0, commandConfig.getMaxShowedAutocompleteCommands());
            if (visibleCommands == 0)
                return;;

            namedCommandsArray.set(cursorIndex, namedCommandsArray.get(cursorIndex).copy().withColor(ClientColors.selectedText));
            height = -(instance.font.lineHeight + textPadding) * visibleCommands;

            drawContext.fill(elementX, elementY, elementX + width, elementY + height, color);
            GraphicsUtil.drawTextList(instance.font, drawContext, elementX + textPadding, elementY + height + textPadding, CommonColors.WHITE, textPadding, namedCommandsArray.toArray(new Component[commandsAvailable]));
        }
    }

    public void handleSystemMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        //Handle fill messages later
    }
}
