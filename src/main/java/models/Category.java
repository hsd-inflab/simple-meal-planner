package models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * provides a set of food categories
 */

public enum Category {
    MEAT, VEGETABLE, FRUIT, SPICE, DAIRY, FATS, SWEETS, BEVERAGE, STARCH, NONE;

    public static final String BUNDLE_NAME = "category";

    public static List<Category> getAll() {
        return List.of(Category.values());
    }

    public String getDisplayName(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        return bundle.getString(this.name());
    }

    public static Map<Category, String> getLocalizedMap(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        Map<Category, String> map = new EnumMap<>(Category.class);
        for (Category cat : values()) {
            map.put(cat, bundle.getString(cat.name()));
        }
        return map;
    }

    public static Map<String, Category> getApiLookupMap(Locale locale) {
        Map<Category, String> displayMap = getLocalizedMap(locale); // liefert Unit -> API-String
        return displayMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,  // API-String als Key
                        Map.Entry::getKey     // Enum als Value
                ));
    }
}
