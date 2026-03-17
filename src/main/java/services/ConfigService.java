package services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * loads and initializes config files
 */
public class ConfigService {
    static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                PROPERTIES.load(input);
            } else {
                throw new RuntimeException("config.properties not found in classpath"); // NOPMD
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e); // NOPMD
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}
