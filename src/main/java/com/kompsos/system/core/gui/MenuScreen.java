package com.kompsos.system.core.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;

public class MenuScreen extends Screen {
    private static final int PADDING = 3, MARGIN = 2;

    private final List<Panel> panels;

    public MenuScreen(Panel... panels) {
        super(Component.literal("Klient"));
        this.panels = List.of(panels);
    }

    @Override
    protected void init() {
        int x = MARGIN, y = MARGIN, rowHeight = 0;

        List<Panel> sorted = panels.stream()
                .sorted(Comparator.<Panel>comparingInt(p -> p.getWidth() * p.getHeight())
                        .thenComparingInt(Panel::getHeight)
                        .reversed())
                .toList();

        for (Panel panel : sorted) {
            if (x + panel.getWidth() > width - PADDING) {
                y += rowHeight + PADDING;
                x = MARGIN;
                rowHeight = 0;
            }

            panel.setX(x);
            panel.setY(y);
            x += panel.getWidth() + PADDING;
            rowHeight = Math.max(rowHeight, panel.getHeight());

            addRenderableWidget(panel);
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return panels.stream().anyMatch(p -> p.keyPressed(event)) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        return panels.stream().anyMatch(p -> p.charTyped(event)) || super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        for (Panel panel : panels) {
            if (!panel.isMouseOver(event.x(), event.y())) panel.blur();
        }
        return handled;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}
}