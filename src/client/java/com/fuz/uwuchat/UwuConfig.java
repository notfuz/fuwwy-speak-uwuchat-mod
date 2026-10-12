package com.fuz.uwuchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UwuConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public boolean translateOutgoing = true;
    public boolean translateIncoming = true;
    public boolean translateServerMessages = false;
    public boolean owoEndings = true;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("uwuchat.json");
    }

    public static UwuConfig load() {
        UwuConfig config = new UwuConfig();
        try {
            if (Files.exists(path())) {
                UwuConfig read = GSON.fromJson(Files.readString(path()), UwuConfig.class);
                if (read != null) config = read;
            }
        } catch (IOException | RuntimeException e) {
            UwuChatMod.LOGGER.warn("Could not read uwuchat.json, using defaults", e);
        }
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException e) {
            UwuChatMod.LOGGER.warn("Could not write uwuchat.json", e);
        }
    }
}
