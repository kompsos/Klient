package com.kompsos.system.core.gui;

import com.kompsos.system.core.moduleloader.managers.ToggleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.CommonColors;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public abstract class Entry {
    public static final int TEXT = CommonColors.WHITE, BODY = 0xFF202020, HOVER = 0xFF303030,
            ON = 0xFF2E7D32, OFF = 0xFF7D2E2E, TRACK = 0xFF1A1A1A, FILL = 0xFF4A6FA5;

    protected final String label;
    protected int x, y, w, h;

    protected Entry(String label) {
        this.label = label;
    }

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    void bounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    int minWidth() {
        return font().width(label);
    }

    void render(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(x, y, x + w, y + h, contains(mx, my) ? HOVER : BODY);
        text(g, label);
    }

    protected void text(GuiGraphicsExtractor g, String s) {
        g.text(font(), s, x + 1, y + 2, TEXT, true);
    }

    void click(double mx, double my, int button) {}

    void drag(double mx) {}

    void blur() {}

    boolean key(KeyEvent event) {
        return false;
    }

    boolean chr(CharacterEvent event) {
        return false;
    }

    public static class Button extends Entry {
        private final Runnable action;

        public Button(String label, Runnable action) {
            super(label);
            this.action = action;
        }

        @Override
        void click(double mx, double my, int button) {
            if (button <= 1) action.run();
        }
    }

    public static class Check extends Entry {
        private final BooleanSupplier state;
        private final Consumer<Boolean> onChange;

        public Check(String label, BooleanSupplier state, Consumer<Boolean> onChange) {
            super(label);
            this.state = state;
            this.onChange = onChange;
        }

        @Override
        void render(GuiGraphicsExtractor g, int mx, int my) {
            g.fill(x, y, x + w, y + h, state.getAsBoolean() ? ON : OFF);
            text(g, label);
        }

        @Override
        void click(double mx, double my, int button) {
            if (button <= 1) onChange.accept(!state.getAsBoolean());
        }
    }

    public static class Toggle extends Entry {
        private final ToggleModule module;

        public Toggle(ToggleModule module) {
            super(module.getId().getPath());
            this.module = module;
        }

        @Override
        void render(GuiGraphicsExtractor g, int mx, int my) {
            g.fill(x, y, x + w, y + h, module.enabled ? ON : OFF);
            text(g, label);
        }

        @Override
        void click(double mx, double my, int button) {
            if (button <= 1) module.toggle();
        }
    }

    public static class Slider extends Entry {
        private final double min, max, def;
        private final DoubleConsumer onChange;
        private double value;

        public Slider(String label, double min, double max, double def, DoubleConsumer onChange) {
            super(label);
            this.min = min;
            this.max = max;
            this.def = def;
            this.value = def;
            this.onChange = onChange;
        }

        public double value() {
            return value;
        }

        @Override
        void render(GuiGraphicsExtractor g, int mx, int my) {
            g.fill(x, y, x + w, y + h, TRACK);
            g.fill(x, y, x + (int) (w * (value - min) / (max - min)), y + h, FILL);
            text(g, contains(mx, my) ? String.format("%.2f", value) : label);
        }

        @Override
        void click(double mx, double my, int button) {
            if (button == 2) set(def);
            else drag(mx);
        }

        @Override
        void drag(double mx) {
            set(min + (max - min) * Math.clamp((mx - x) / w, 0.0, 1.0));
        }

        private void set(double v) {
            value = v;
            onChange.accept(v);
        }
    }


    public static class Input extends Entry {
        private final Consumer<String> onDone;
        private String value;
        private EditBox box;
        private boolean editing;

        public Input(String label, String value, Consumer<String> onDone) {
            super(label);
            this.value = value == null ? "" : value;
            this.onDone = onDone;
        }

        public String value() {
            return value;
        }

        /** Resets the displayed value (e.g. after a rejected edit). */
        public void set(String v) {
            this.value = v == null ? "" : v;
        }

        private EditBox box() {
            if (box == null) {
                box = new EditBox(font(), 0, 0, 10, 10, Component.literal(label));
                box.setBordered(false);
                box.setMaxLength(256);
            }
            return box;
        }

        private void commit() {
            if (!editing) return;

            editing = false;
            EditBox b = box();
            String text = b.getValue();
            b.setFocused(false);

            if (!value.equals(text)) {
                value = text;
                onDone.accept(value);
            }
        }

        @Override
        int minWidth() {
            return font().width(label + ": " + value) + 6;
        }

        @Override
        void render(GuiGraphicsExtractor g, int mx, int my) {
            g.fill(x, y, x + w, y + h, editing ? HOVER : BODY);

            if (editing) {
                EditBox b = box();
                b.setX(x + 1);
                b.setY(y + 1);
                b.setWidth(w - 2);
                b.setHeight(h - 1);
                b.extractRenderState(g, mx, my, 0f);
            } else {
                text(g, font().plainSubstrByWidth(label + ": " + value, w - 3, true));
            }
        }

        @Override
        void click(double mx, double my, int button) {
            if (button > 1 || editing) return;

            editing = true;
            EditBox b = box();
            b.setValue(value);
            b.setFocused(true);
            b.moveCursorToEnd(false);
        }

        @Override
        void blur() {
            commit();
        }

        @Override
        boolean key(KeyEvent event) {
            if (!editing) return false;

            if (event.isConfirmation()) {
                commit();
                return true;
            }

            return box().keyPressed(event);
        }

        @Override
        boolean chr(CharacterEvent event) {
            return editing && box().charTyped(event);
        }
    }

}