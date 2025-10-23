package services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * loads and initializes config files
 */

public class ConfigService {
    static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("config.properties not found in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
