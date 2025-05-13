package util;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class UnitConversionHelperTest {
    double conversionTolerance = 0.02;

    // --- convert(String fromUnit, String toUnit, double amount) ---

    @ParameterizedTest
    @MethodSource("validSimpleConversionCases")
    void testConvertSimpleValid(String from, String to, double amount, double expected) {
        double result = UnitConversionHelper.convert(from, to, amount);
        assertEquals(expected, result, conversionTolerance);
    }

    @ParameterizedTest
    @MethodSource("invalidSimpleConversionCases")
    void testConvertSimpleInvalid(String from, String to, double amount) {
        assertThrows(IllegalArgumentException.class, () ->
                UnitConversionHelper.convert(from, to, amount)
        );
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> validSimpleConversionCases() {
        return Stream.of(
                of("ml", "l", 500, 0.5),  // Beispiel: ml → l
                of("centiliters", "esslöffel", 15, 10),
                of("deZIliter", "MUG", 2.5, 1),
                of("glasses", "kg", 20, 4),
                of("dag", "teaspoon", 10, 20),
                of("tasse", "g", 3, 600),
                of("pfund", "oz", 1, 16)
        );
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> invalidSimpleConversionCases() {
        return Stream.of(
                of("foo", "l", 100)  // Ungültige Einheit
        );
    }

    // --- convert(String fromUnit, String toUnit, double amount, String ingredient) ---

    @ParameterizedTest
    @MethodSource("validIngredientConversionCases")
    void testConvertIngredientValid(String from, String to, double amount, String ingredient, double expected) {
        double result = UnitConversionHelper.convert(from, to, amount, ingredient);
        assertEquals(expected, result, conversionTolerance);
    }

    @ParameterizedTest
    @MethodSource("invalidIngredientConversionCases")
    void testConvertIngredientInvalid(String from, String to, double amount, String ingredient) {
        assertThrows(IllegalArgumentException.class, () ->
                UnitConversionHelper.convert(from, to, amount, ingredient)
        );
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> validIngredientConversionCases() {
        return Stream.of(
                of("g", "l", 1000, "wasser", 1.0),  // Beispiel: g → l für wasser
                of("becher", "kg", 2, "flour", 0.35),
                of("tsp", "g", 1, "zucker", 5*0.85),
                of("dl", "deka", 1, "öl", 9.2),
                of("kg", "tasse", 1, "milk", 5/1.03)
        );
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> invalidIngredientConversionCases() {
        return Stream.of(
                of("bar", "kg", 42, "öl")  // Ungültige Einheit
        );
    }
}
