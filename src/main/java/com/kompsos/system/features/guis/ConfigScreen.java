package com.kompsos.system.features.guis;

import com.google.gson.annotations.SerializedName;
import com.kompsos.system.core.config.ConfigLoader;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.gui.Entry;
import com.kompsos.system.core.gui.MenuScreen;
import com.kompsos.system.core.gui.Panel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfigScreen extends MenuScreen {

    public ConfigScreen(ConfigLoader loader) {
        super(build(loader));
    }

    private static Panel[] build(ConfigLoader loader) {
        KlientConfig root = loader.getConfigObject();
        List<Panel> panels = new ArrayList<>();
        collect(loader, "Klient", root, panels);
        return panels.toArray(new Panel[0]);
    }

    private static void collect(ConfigLoader loader, String title, Object target, List<Panel> out) {
        List<Entry> entries = new ArrayList<>();

        for (Field field : target.getClass().getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isSynthetic()) continue;

            Class<?> type = field.getType();
            if (type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)
                    || type.isEnum()) continue;

            try {
                field.setAccessible(true);

                if (isLeaf(type)) {
                    entries.add(entryFor(loader, target, field));
                } else {
                    Object child = field.get(target);
                    if (child != null) collect(loader, nameOf(field), child, out);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                e.printStackTrace();
            }
        }

        if (!entries.isEmpty()) {
            out.add(new Panel(title).add(entries.toArray(new Entry[0])));
        }
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
                    v -> {
                        write(loader, target, field, v);
                    });
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