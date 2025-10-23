package util;

import java.util.Map;

@Deprecated
public abstract class UnitConversionHelper {

    private static final Map<String, Double> ingredientDensities = Map.ofEntries(
            Map.entry("wasser", 1.0),
            Map.entry("water", 1.0),
            Map.entry("zucker", 0.85),     // Zucker: 1 l ≈ 850 g
            Map.entry("sugar", 0.85),
            Map.entry("mehl", 0.7),        // Mehl: 1 l ≈ 600 g
            Map.entry("flour", 0.7),
            Map.entry("butter", 0.91),     // Butter: 1 l ≈ 910 g
            Map.entry("oil", 0.92),        // Öl: 1 l ≈ 920 g
            Map.entry("öl", 0.92),
            Map.entry("milch", 1.03),      // Milch: 1 l ≈ 1030 g
            Map.entry("milk", 1.03)
    );


    public static double convert(String fromUnit, String toUnit, double amount) {
        double fromFactor = getFactor(fromUnit.toLowerCase());
        double toFactor = getFactor(toUnit.toLowerCase());
        return (amount * fromFactor) / toFactor;
    }

    public static double convert(String fromUnit, String toUnit, double amount, String ingredient) {
        double fromFactor = getFactor(fromUnit.toLowerCase());
        double toFactor = getFactor(toUnit.toLowerCase());
        boolean isFromVol = isVolumeUnit(fromUnit);
        boolean isToVol = isVolumeUnit(toUnit);

        double density = ingredientDensities.getOrDefault(ingredient.toLowerCase(), 1.0); // Standard = Wasser

        if (isFromVol && !isToVol) {
            // Volumen → Gewicht
            return (amount * fromFactor) * density / toFactor;
        } else if (!isFromVol && isToVol) {
            // Gewicht → Volumen
            return (amount * fromFactor) / density / toFactor;
        } else {
            // Gleichartig wie oben
            return (amount * fromFactor) / toFactor;
        }
    }

    private static double getFactor(String unit) {
        return switch (unit) {
            // Volumenmaße (Liter)
            case "ml", "milliliter", "milliliters" -> 0.001;
            case "cl", "centiliter", "centiliters" -> 0.01;
            case "dl", "deziliter", "deziliters" -> 0.1;
            case "l", "liter", "litre", "liters", "litres" -> 1.0;
            case "tl", "teelöffel", "tsp", "teaspoon", "teaspoons" -> 0.005;
            case "el", "esslöffel", "tbsp", "tablespoon", "tablespoons" -> 0.015;
            case "tasse", "tassen", "cup", "cups" -> 0.2;
            case "becher", "mug", "mugs" -> 0.25;
            case "glas", "glass", "glasses" -> 0.2;

            // Gewichtsmaße (Kilogramm)
            case "g", "gramm", "gram", "grams" -> 0.001;
            case "kg", "kilogramm", "kilogram", "kilograms" -> 1.0;
            case "dag", "deka", "dekagramm", "dekagram", "dekagrams" -> 0.01;
            case "oz", "ounce", "ounces", "unze", "unzen" -> 0.02835;
            case "lb", "pound", "pounds", "pfund", "pfunde" -> 0.454;

            default -> throw new IllegalArgumentException("Unbekannte Einheit: " + unit);
        };
    }

    private static boolean isVolumeUnit(String unit) {
        return switch (unit.toLowerCase()) {
            case "ml", "milliliter", "milliliters",
                 "cl", "centiliter", "centiliters",
                 "dl", "deziliter", "deziliters",
                 "l", "liter", "litre", "liters", "litres",
                 "tl", "teelöffel", "tsp", "teaspoon", "teaspoons",
                 "el", "esslöffel", "tbsp", "tablespoon", "tablespoons",
                 "tasse", "tassen", "cup", "cups",
                 "becher", "mug", "mugs",
                 "glas", "glass", "glasses" -> true;
            default -> false;
        };
    }
}
