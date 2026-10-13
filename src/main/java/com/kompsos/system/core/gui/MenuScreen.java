package com.kompsos.system.core.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MenuScreen extends Screen {
    private static final int PADDING = 3, MARGIN = 2;

    private final List<Panel> panels;

    public MenuScreen(Panel... panels) {
        super(Component.literal("Klient"));
        Set<Panel> all = new LinkedHashSet<>();
        for (Panel p : panels) if (p != null) collect(p, all); // skip categories with nothing editable
        this.panels = List.copyOf(all);
    }

    /** Gathers a panel and every panel reachable through its PanelToggle entries. */
    private static void collect(Panel panel, Set<Panel> out) {
        if (out.add(panel)) panel.linked().forEach(c -> collect(c, out));
    }

    @Override
    protected void init() {
        for (Panel panel : panels) addRenderableWidget(panel);
        relayout();
    }

    /** Forgets every panel position (dragged or not) and packs all visible panels from scratch. */
    public void resetLayout() {
        for (Panel p : panels) {
            p.placed = false;
            p.moved = false;
        }
        relayout();
    }

    /**
     * Gives every visible panel that has no position yet the first free spot,
     * scanning top-left to bottom-right. Panels that already have a position
     * (including ones the user dragged) stay where they are, apart from being
     * clamped back onto the screen if the window shrank.
     */
    public void relayout() {
        List<Panel> placed = new ArrayList<>();
        List<Panel> pending = new ArrayList<>();

        for (Panel p : panels) {
            if (!p.visible) continue;
            if (p.placed) {
                clamp(p);
                placed.add(p);
            } else {
                pending.add(p);
            }
        }

        pending.sort(Comparator.<Panel>comparingInt(p -> p.getWidth() * p.getHeight())
                .thenComparingInt(Panel::getHeight)
                .reversed());

        for (Panel panel : pending) {
            int[] spot = findSpot(panel, placed);
            panel.setX(spot[0]);
            panel.setY(spot[1]);
            panel.placed = true;
            placed.add(panel);
        }
    }

    private void clamp(Panel p) {
        int x = Math.max(0, Math.min(p.getX(), width - p.getWidth()));
        int y = Math.max(0, Math.min(p.getY(), height - p.getHeight()));
        if (x != p.getX()) p.setX(x);
        if (y != p.getY()) p.setY(y);
    }

    private int[] findSpot(Panel panel, List<Panel> others) {
        int w = panel.getWidth(), h = panel.getHeight();

        for (int y = MARGIN; y + h <= height; y++) {
            for (int x = MARGIN; x + w <= width - PADDING; x++) {
                if (isFree(x, y, w, h, others)) return new int[]{x, y};
            }
        }

        return new int[]{MARGIN, MARGIN}; // screen is full, stack at the corner
    }

    private static boolean isFree(int x, int y, int w, int h, List<Panel> others) {
        for (Panel o : others) {
            boolean overlaps = x < o.getX() + o.getWidth() + PADDING
                    && x + w + PADDING > o.getX()
                    && y < o.getY() + o.getHeight() + PADDING
                    && y + h + PADDING > o.getY();
            if (overlaps) return false;
        }
        return true;
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
        // Middle click on empty space (not over any visible panel) resets the whole layout.
        if (event.button() == 2) {
            boolean overPanel = panels.stream().anyMatch(p -> p.isMouseOver(event.x(), event.y()));
            if (!overPanel) {
                resetLayout();
                return true;
            }
        }

        boolean handled = super.mouseClicked(event, doubleClick);
        for (Panel panel : panels) {
            if (!panel.isMouseOver(event.x(), event.y())) panel.blur();
        }
        return handled;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}
}
