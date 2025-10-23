package models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * provides a set of often used units in cooking
 */

public enum Unit {
    ML, CL, DL, L, G, KG, TSP, TBSP, CUP, UNIT, PINCH, NONE;

    public static final String BUNDLE_NAME = "unit";


    public static List<Unit> getAll() {
        return List.of(Unit.values());
    }

    public String getDisplayName(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        return bundle.getString(this.name());
    }

    public static Map<Unit, String> getLocalizedMap(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        Map<Unit, String> map = new EnumMap<>(Unit.class);
        for (Unit unit : values()) {
            map.put(unit, bundle.getString(unit.name()));
        }
        return map;
    }

    public static Map<String, Unit> getApiLookupMap(Locale locale) {
        Map<Unit, String> displayMap = getLocalizedMap(locale); // liefert Unit -> API-String
        return displayMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,  // API-String als Key
                        Map.Entry::getKey     // Enum als Value
                ));
    }
}
