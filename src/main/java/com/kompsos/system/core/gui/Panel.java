package com.kompsos.system.core.gui;

import com.kompsos.system.lib.ClientColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Panel extends AbstractWidget {
    private static final int BACKGROUND = ClientColors.backgroundColor, BORDER = ClientColors.selectedText, GRABBED = ClientColors.selectedBorder;

    private final List<Entry> entries = new ArrayList<>();
    private Entry active;
    private boolean grabbed;
    private int grabX, grabY;

    /** True once MenuScreen has given this panel a position (or the user dragged it). */
    boolean placed;
    /** True once the user has dragged this panel; its position is then kept. */
    boolean moved;

    public boolean draggable = true;

    public Panel(String title) {
        super(0, 0, 0, 0, Component.literal(title));
        fit();
    }

    public Panel add(Entry... added) {
        entries.addAll(List.of(added));
        fit();
        return this;
    }

    public void blur() {
        if (active != null) active.blur();
        active = null;
    }

    /** Panels owned by PanelToggle entries in this panel. */
    List<Panel> linked() {
        List<Panel> out = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry instanceof Entry.PanelToggle t) out.add(t.panel());
        }
        return out;
    }

    /**
     * Hides this panel and any panels it opened. A panel the user dragged keeps its
     * position for next time; an undragged one gets a fresh free spot when shown again.
     */
    public void hide() {
        blur();
        grabbed = false;
        visible = false;
        if (!moved) placed = false;
        for (Panel child : linked()) child.hide();
    }

    private int row() {
        return Entry.font().lineHeight + 2;
    }

    private void fit() {
        int wide = Entry.font().width(getMessage());
        for (Entry entry : entries) wide = Math.max(wide, entry.minWidth());

        setWidth(wide + 5);
        setHeight(row() * (entries.size() + 1) + 1);
        layout();
    }

    private void layout() {
        int row = row();
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).bounds(getX() + 1, getY() + row * (i + 1) + 1, getWidth() - 2, row - 1);
        }
    }

    private Entry entryAt(double mx, double my) {
        for (Entry entry : entries) if (entry.contains(mx, my)) return entry;
        return null;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layout();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layout();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 2 && isMouseOver(event.x(), event.y())) {
            onClick(event, doubleClick);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
        Font font = Entry.font();
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int border = grabbed ? GRABBED : BORDER;

        layout();
        g.fill(x, y, x + w, y + h, BACKGROUND);
        g.text(font, getMessage(), x + 1, y + 2, Entry.TEXT, true);
        g.outline(x - 1, y - 1, w + 2, h + 2, border);
        g.fill(x, y + font.lineHeight + 1, x + w, y + font.lineHeight + 2, border);

        for (Entry entry : entries) entry.render(g, mx, my);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Entry hit = entryAt(event.x(), event.y());

        if (active != null && active != hit) active.blur();
        active = hit;

        if (active != null) {
            active.click(event.x(), event.y(), event.button());
            return;
        }

        grabX = (int) event.x() - getX();
        grabY = (int) event.y() - getY();
        grabbed = true;
    }

    @Override
    protected void onDrag(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (active != null) {
            active.drag(event.x());
        } else if (draggable) {
            setX((int) event.x() - grabX);
            setY((int) event.y() - grabY);
            moved = true;
            placed = true;
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        grabbed = false;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return active != null && active.key(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        return active != null && active.chr(event);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
}