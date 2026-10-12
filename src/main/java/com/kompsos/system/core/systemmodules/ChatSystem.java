package com.kompsos.system.core.systemmodules;

import com.kompsos.Klient;
import com.kompsos.mixin.ChatScreenAccessor;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Command;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.lib.ClientColors;
import com.kompsos.system.lib.GraphicsUtil;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.CommonColors;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

public class ChatSystem extends Module {

    public ChatSystem() {
        super(ModuleManager.ExecutorMode.SINGLE_MAIN, Klient.id("chat_system"));
    }

    @Override
    public void clientReady() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // If the screen is the active Chat Input screen, hook into its render pass
            if (screen instanceof ChatScreen) {
                ScreenEvents.afterExtract(screen).register((chatScreen, guiGraphicsExtractor, _, _, _) -> handleAutocomplete(client, scaledWidth, scaledHeight, guiGraphicsExtractor, chatScreen));
            }
        });
    }

    public void handleClientsideChatMessage(String msg, CallbackInfo ci) {
        KlientConfig.Commands commandConfig = Klient.configLoader.getConfigObject().commands;

        if (commandConfig.getCommandsEnabled() && msg.startsWith(commandConfig.getPrefix())) {
            Klient.commandManager.processChatCommand(msg, ci);
        }
    }

    private final int cursorIndex = 0;

    public void handleAutocomplete(Minecraft instance, int scaledWidth, int scaledHeight, GuiGraphicsExtractor drawContext, Screen screen) {
        KlientConfig.Commands commandConfig = Klient.configLoader.getConfigObject().commands;
        if (!commandConfig.getCommandsEnabled())
            return;

        int visibleCommands;

        //For autocomplete box
        int color = ClientColors.backgroundColor;
        int elementX = 3;
        int elementY = scaledHeight - 16;
        int textPadding = 2;
        int width = 225;
        int height;


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

            visibleCommands = Math.clamp(commandsAvailable, 0, commandConfig.getMaxAutoCmds());
            if (visibleCommands == 0)
                return;

            namedCommandsArray.set(cursorIndex, namedCommandsArray.get(cursorIndex).copy().withColor(ClientColors.selectedText));
            height = -(instance.font.lineHeight + textPadding) * visibleCommands;

            drawContext.fill(elementX, elementY, elementX + width, elementY + height, color);
            drawContext.outline(elementX, elementY, width, height, CommonColors.WHITE);
            GraphicsUtil.drawTextList(instance.font, drawContext, elementX + textPadding, elementY + height + textPadding, CommonColors.WHITE, textPadding, namedCommandsArray.toArray(new Component[commandsAvailable]));

            drawInfoBox(drawContext, scaledWidth, scaledHeight, instance, namedCommandsArray.toArray(new Component[commandsAvailable]));
        }
    }

    private void drawInfoBox(GuiGraphicsExtractor drawContext, int scaledWidth, int scaledHeight, Minecraft instance, Component[] commandNameList) {
        int x = scaledWidth - 4;
        int y = scaledHeight - 16;
        int width = scaledWidth - 198;
        int height = scaledHeight - 100;

        int boxWidth = 195;
        int boxHeight = 85;
        int textX = width + 2;
        int textY = height + 2;
        int textWidth = boxWidth - 6;

        drawContext.fill(x, y, width, height, ClientColors.backgroundColor);
        drawContext.outline(x - 1, height - 1, -193, boxHeight, ClientColors.selectedText);
        drawContext.horizontalLine(x - 1, width, height + instance.font.lineHeight + 2, ClientColors.selectedText);

        drawContext.text(
                instance.font, commandNameList[cursorIndex], textX, textY, CommonColors.WHITE);

        Component description = Component.literal(Klient.commandManager
                .getCommand(commandNameList[cursorIndex].getString())
                .getDescription());

        int descriptionY = textY + 12;
        int availableHeight = boxHeight - 12;
        int maxLines = availableHeight / instance.font.lineHeight;

        var lines = instance.font.split(description, textWidth);

        for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
            drawContext.text(instance.font, lines.get(i), textX, descriptionY + i * instance.font.lineHeight, CommonColors.WHITE);
        }
    }

    public void handleSystemMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        //Handle fill messages later
    }
}