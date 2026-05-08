package remoteagent.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        reload();
    }

    public static synchronized void reload() {
        properties.clear();

        try (InputStream input =
                     Config.class.getClassLoader()
                             .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new RuntimeException(CONFIG_FILE + " not found");
            }

            properties.load(input);

            System.out.println("Config reloaded.");

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