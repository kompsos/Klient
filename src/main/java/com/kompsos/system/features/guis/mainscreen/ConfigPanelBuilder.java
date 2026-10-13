package com.kompsos.system.features.guis.mainscreen;

import com.google.gson.annotations.SerializedName;
import com.kompsos.Klient;
import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.gui.Entry;
import com.kompsos.system.core.gui.Panel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfigPanelBuilder {

    /**
     * Builds the panel for a single config category, e.g. {@code build(loader, "Commands")}.
     * The category is matched against the section's JSON name or its field name, ignoring case.
     * Sections nested inside the category become their own panels, opened through a
     * {@link Entry.PanelToggle} entry.
     *
     * @return the panel, or null if there is no such category or it has nothing editable
     */
    public static Panel build(ConfigLoader loader, String category) {
        KlientConfig root = loader.getConfigObject();

        for (Field field : fieldsOf(root)) {
            if (isLeaf(field.getType())) continue;
            if (!matches(field, category)) continue;

            try {
                Object section = field.get(root);
                return section == null ? null : panelFor(loader, nameOf(field), section);
            } catch (IllegalAccessException e) {
                Klient.LOGGER.error(e.getMessage());
                return null;
            }
        }

        return null;
    }

    /** Names of every category that {@link #build} accepts. */
    public static List<String> categories(ConfigLoader loader) {
        List<String> names = new ArrayList<>();
        for (Field field : fieldsOf(loader.getConfigObject())) {
            if (!isLeaf(field.getType())) names.add(nameOf(field));
        }
        return names;
    }

    private static Panel panelFor(ConfigLoader loader, String title, Object target) {
        List<Entry> entries = new ArrayList<>();

        for (Field field : fieldsOf(target)) {
            Class<?> type = field.getType();

            try {
                if (isLeaf(type)) {
                    entries.add(entryFor(loader, target, field));
                } else {
                    Object child = field.get(target);
                    if (child == null) continue;

                    Panel childPanel = panelFor(loader, nameOf(field), child);
                    if (childPanel != null) entries.add(new Entry.PanelToggle(nameOf(field), childPanel));
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                Klient.LOGGER.error(e.getMessage());
            }
        }

        if (entries.isEmpty()) return null;
        return new Panel(title).add(entries.toArray(new Entry[0]));
    }

    /** Editable fields of an object: no statics, transients, arrays, collections, maps or enums. */
    private static List<Field> fieldsOf(Object target) {
        List<Field> out = new ArrayList<>();

        for (Field field : target.getClass().getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isSynthetic()) continue;

            Class<?> type = field.getType();
            if (type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)
                    || type.isEnum()) continue;

            try {
                field.setAccessible(true);
                out.add(field);
            } catch (RuntimeException e) {
                Klient.LOGGER.error(e.getMessage());
            }
        }

        return out;
    }

    private static boolean matches(Field field, String category) {
        return nameOf(field).equalsIgnoreCase(category) || field.getName().equalsIgnoreCase(category);
    }

    private static boolean isLeaf(Class<?> t) {
        return t.isPrimitive() || t == String.class || Number.class.isAssignableFrom(t)
                || t == Boolean.class || t == Character.class;
    }

    private static String nameOf(Field f) {
        SerializedName sn = f.getAnnotation(SerializedName.class);
        return sn != null ? sn.value() : f.getName();
    }

    private static Entry entryFor(ConfigLoader loader, Object target, Field field) throws IllegalAccessException {
        Class<?> type = field.getType();
        String label = field.getName();

        if (type == boolean.class || type == Boolean.class) {
            return new Entry.Check(label,
                    () -> (boolean) read(target, field),
                    v -> write(loader, target, field, v));
        }

        Entry.Input[] self = new Entry.Input[1];
        self[0] = new Entry.Input(label, String.valueOf(field.get(target)), text -> {
            try {
                write(loader, target, field, parse(type, text));
            } catch (RuntimeException e) {
                // invalid input: put the real value back in the box
                self[0].set(String.valueOf(read(target, field)));
            }
        });
        return self[0];
    }

    private static Object parse(Class<?> t, String s) {
        s = s.trim();
        if (t == int.class || t == Integer.class) return Integer.parseInt(s);
        if (t == long.class || t == Long.class) return Long.parseLong(s);
        if (t == double.class || t == Double.class) return Double.parseDouble(s);
        if (t == float.class || t == Float.class) return Float.parseFloat(s);
        if (t == short.class || t == Short.class) return Short.parseShort(s);
        if (t == byte.class || t == Byte.class) return Byte.parseByte(s);
        if (t == char.class || t == Character.class) {
            if (s.length() != 1) throw new IllegalArgumentException("expected one character");
            return s.charAt(0);
        }
        return s; // String
    }

    private static Object read(Object target, Field field) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(ConfigLoader loader, Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        loader.save();
    }
}