package com.kompsos.system.core.config;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

public class ConfigLoader {
    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Created once and never replaced: load() copies new values into this
    // same instance, so every reference to it (and to its nested sections)
    // always sees the current values.
    private final KlientConfig config = new KlientConfig();

    public ConfigLoader(Path configPath){
        this.configPath = configPath;
        this.load();
    }

    /** Reads the file and applies its values onto the live config object. */
    public void load() {
        File configFile = new File(configPath.toUri());

        if(configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);

                if (jsonElement.isJsonObject()) {
                    KlientConfig loaded = gson.fromJson(jsonElement, KlientConfig.class);
                    if (loaded != null) copyInto(config, loaded);
                    save(); // write back so newly added fields appear in the file
                } else {
                    ensureExisting(configFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                ensureExisting(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public KlientConfig getConfigObject() {
        return this.config;
    }

    public void save() {
        try (Writer writer = new FileWriter(configPath.toFile())) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureExisting(File configFile) throws IOException {
        File parent = configFile.getParentFile();
        if (parent != null) parent.mkdirs();
        if (!configFile.exists()) configFile.createNewFile();

        copyInto(config, new KlientConfig()); // reset to defaults, in place
        save();
    }

    /** Copies every field of src into dst, recursing into nested sections so dst's section objects are kept. */
    private static void copyInto(Object dst, Object src) {
        for (Field f : dst.getClass().getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || f.isSynthetic()) continue;

            try {
                f.setAccessible(true);
                Object from = f.get(src);
                Object to = f.get(dst);
                Class<?> t = f.getType();

                boolean leaf = t.isPrimitive() || t == String.class || Number.class.isAssignableFrom(t)
                        || t == Boolean.class || t == Character.class || t.isEnum()
                        || t.isArray() || java.util.Collection.class.isAssignableFrom(t)
                        || java.util.Map.class.isAssignableFrom(t);

                if (!leaf && from != null && to != null) {
                    copyInto(to, from);
                } else if (from != null || !t.isPrimitive()) {
                    f.set(dst, from);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
