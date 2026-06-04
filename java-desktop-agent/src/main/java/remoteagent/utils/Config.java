package remoteagent.utils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        reload();
    }

    static File file = null;

    public static void openConfigFile() {

        file = Paths.get(CONFIG_FILE).toFile();

        if (!file.exists()) {
            throw new IllegalArgumentException("Config file not found: " + file.getAbsolutePath());
        }

        try {
            Desktop.getDesktop().open(file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void reload() {
        properties.clear();

        file = Paths.get(CONFIG_FILE).toFile();

        if (!file.exists()) {
            throw new RuntimeException("Config file not found: " + file.getAbsolutePath());
        }

        try (InputStream input = new FileInputStream(file)) {

            properties.load(input);

            System.out.println("Config reloaded from: " + file.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Failed to reload config", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}