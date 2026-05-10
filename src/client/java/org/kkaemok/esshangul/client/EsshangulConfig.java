package org.kkaemok.esshangul.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class EsshangulConfig {
    private static final String KEY_ENABLED = "enabled";
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("esshangul.properties");

    private static volatile boolean enabled = true;

    private EsshangulConfig() {
    }

    public static void load() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                properties.load(in);
            } catch (IOException ignored) {
            }
        }

        enabled = Boolean.parseBoolean(properties.getProperty(KEY_ENABLED, "true"));
        applySideEffects();
        save();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        applySideEffects();
        save();
    }

    private static void applySideEffects() {
        EssentialImeBridge.onConfigChanged(enabled);
        EssentialPreeditOverlay.onConfigChanged(enabled);
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(KEY_ENABLED, Boolean.toString(enabled));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(out, "EssHangul settings");
            }
        } catch (IOException ignored) {
        }
    }
}
