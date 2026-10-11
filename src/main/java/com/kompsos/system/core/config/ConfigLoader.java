package com.kompsos.system.core.config;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Path;

public class ConfigLoader {
    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private KlientConfig config;

    public ConfigLoader(Path configPath){
        this.configPath = configPath;
        this.load();
    }

    public void load() {
        File configFile = new File(configPath.toUri());

        if(configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);

                if (jsonElement.isJsonObject()) {
                    config = gson.fromJson(jsonElement, KlientConfig.class);
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
        configFile.mkdirs();
        configFile.delete();
        configFile.createNewFile();

        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(new KlientConfig(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}