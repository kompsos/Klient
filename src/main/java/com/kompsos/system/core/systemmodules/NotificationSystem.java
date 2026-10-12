package com.kompsos.system.core.systemmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import com.kompsos.system.lib.ClientColors;
import com.kompsos.system.lib.MathUtil;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.*;

public class NotificationSystem extends Module {
    private static final int ANIMATION_TIME_MS = 30;
    private static final int NOTIFICATION_WIDTH = 115;
    private static final int NOTIFICATION_HEIGHT = 52;
    private static final int SCREEN_PADDING = 10;

    private final List<Notification> notifications = new ArrayList<>();
    private boolean ready = false;

    public NotificationSystem() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("notification_system"));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!ready) {
                    return;
                }

                Iterator<Notification> iterator = notifications.iterator();

                while (iterator.hasNext()) {
                    Notification notification = iterator.next();
                    notification.elapsedTimeMs++;

                    if (notification.elapsedTimeMs >= notification.totalTime + ANIMATION_TIME_MS * 2) {
                        iterator.remove();
                    }
                }
            }
        }, 0, 50);
    }

    @Override
    public void clientReady() {
        ready = true;

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Klient.id("notification_system_hud"),
                (graphicsExtractor, _) -> {
                    if (Minecraft.getInstance().gui.screen() == null) {
                        renderNotifications(graphicsExtractor);
                    }
                }
        );

        ScreenEvents.AFTER_INIT.register((_, screen, _, _) ->
                ScreenEvents.afterExtract(screen).register((_, graphicsExtractor, _, _, _) ->
                        renderNotifications(graphicsExtractor)
                )
        );

        notify(Component.literal("Klient has successfuly loaded"), 80);
    }

    public void renderNotifications(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);

            int x = getX(notification);
            int y = SCREEN_PADDING + i * (NOTIFICATION_HEIGHT + 5);

            int width = x + NOTIFICATION_WIDTH;
            int height = y + NOTIFICATION_HEIGHT;

            graphics.fill(x, y, width, height, ClientColors.backgroundColor);
            graphics.outline(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, ClientColors.selectedText);

            graphics.text(
                    Minecraft.getInstance().font,
                    Component.literal("Klient").withColor(ClientColors.selectedText),
                    x + 2,
                    y + 2,
                    0xFFFFFFFF
            );

            int textX = x + 2;
            int textY = y + 14;
            int maxTextWidth = NOTIFICATION_WIDTH - 4;
            int maxTextHeight = NOTIFICATION_HEIGHT - 16;
            int lineHeight = Minecraft.getInstance().font.lineHeight;
            int maxLines = maxTextHeight / lineHeight;

            List<net.minecraft.util.FormattedCharSequence> lines =
                    Minecraft.getInstance().font.split(notification.getMessage(), maxTextWidth);

            for (int line = 0; line < Math.min(lines.size(), maxLines); line++) {
                graphics.text(
                        Minecraft.getInstance().font,
                        lines.get(line),
                        textX,
                        textY + line * lineHeight,
                        0xFFFFFFFF
                );
            }
        }
    }

    private static int getX(Notification notification) {
        float progress = notification.elapsedTimeMs;
        float eased;

        if (progress < ANIMATION_TIME_MS) {
            eased = (float) MathUtil.easeOutCubic(progress / ANIMATION_TIME_MS);
        } else if (progress < notification.totalTime + ANIMATION_TIME_MS) {
            eased = 1.0f;
        } else {
            float exitProgress = (progress - notification.totalTime - ANIMATION_TIME_MS)
                    / ANIMATION_TIME_MS;
            eased = 1.0f - (float) MathUtil.easeOutCubic(exitProgress);
        }

        eased = Math.clamp(eased, 0.0f, 1.0f);

        return (int) (SCREEN_PADDING - NOTIFICATION_WIDTH * (1.07f - eased));
    }

    public void push(int liveTime, Component message) {
        notifications.add(new Notification(liveTime, 0, message));
    }

    class Notification {
        private final int totalTime;
        private int elapsedTimeMs;
        private final Component message;

        public Notification(int totalTime, int elapsedTimeMs, Component message) {
            this.totalTime = Math.max(0, totalTime);
            this.elapsedTimeMs = elapsedTimeMs;
            this.message = message;
        }

        public Component getMessage() {
            return message;
        }

    }
}