package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hsd.inflab.smp.dto.response.EnumOptionDto;
import hsd.inflab.smp.dto.response.MetadataResponseDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testklasse fuer den MetadataService.
 *
 * Ziel:
 * - Prueft, dass fuer jeden Enum-Wert von Unit und Category eine deutsche
 *   Bezeichnung ausgeliefert wird.
 * - Prueft, dass die Reihenfolge der Enum-Deklaration erhalten bleibt.
 *
 * Der Service ist zustandslos und benoetigt keine Mocks.
 */
class MetadataServiceTest {

    private final MetadataService metadataService = new MetadataService();

    /**
     * Erwartung:
     * - Es gibt genau so viele Unit-Eintraege wie Enum-Werte.
     * - Der Rohwert "ML" wird auf das Label "ml" abgebildet.
     */
    @Test
    void getMetadata_returnsAllUnitsWithGermanLabels() {
        // Arrange: kein Setup noetig, Service ist zustandslos.

        // Act: Metadaten abrufen.
        MetadataResponseDto metadata = metadataService.getMetadata();

        // Assert: Anzahl und Beispiel-Mapping pruefen.
        assertEquals(Unit.values().length, metadata.units().size());
        assertEquals("ml", findLabel(metadata.units(), "ML"));
    }

    /**
     * Erwartung:
     * - Es gibt genau so viele Category-Eintraege wie Enum-Werte.
     * - Der Rohwert "MEAT" wird auf das Label "Fleisch" abgebildet.
     */
    @Test
    void getMetadata_returnsAllCategoriesWithGermanLabels() {
        // Arrange: kein Setup noetig, Service ist zustandslos.

        // Act: Metadaten abrufen.
        MetadataResponseDto metadata = metadataService.getMetadata();

        // Assert: Anzahl und Beispiel-Mapping pruefen.
        assertEquals(Category.values().length, metadata.categories().size());
        assertEquals("Fleisch", findLabel(metadata.categories(), "MEAT"));
    }

    /**
     * Erwartung:
     * - Die Listen behalten die Reihenfolge der Enum-Deklaration bei,
     *   damit das Frontend stabile Dropdown-Reihenfolgen erhaelt.
     */
    @Test
    void getMetadata_preservesEnumDeclarationOrder() {
        // Arrange: kein Setup noetig, Service ist zustandslos.

        // Act: Metadaten abrufen.
        MetadataResponseDto metadata = metadataService.getMetadata();

        // Assert: Der erste Eintrag entspricht dem ersten Enum-Wert.
        assertEquals(Unit.values()[0].name(), metadata.units().get(0).value());
        assertEquals(Category.values()[0].name(), metadata.categories().get(0).value());
    }

    /**
     * Erwartung:
     * - getUnits liefert genau so viele Eintraege wie Unit-Enumwerte,
     *   in Enum-Reihenfolge, mit korrektem Label (ML -> "ml").
     */
    @Test
    void getUnits_returnsAllUnitsWithGermanLabels() {
        // Arrange: kein Setup noetig, Service ist zustandslos.

        // Act: nur die Units abrufen.
        List<EnumOptionDto> units = metadataService.getUnits();

        // Assert: Anzahl, Reihenfolge und Beispiel-Mapping pruefen.
        assertEquals(Unit.values().length, units.size());
        assertEquals(Unit.values()[0].name(), units.get(0).value());
        assertEquals("ml", findLabel(units, "ML"));
    }

    /**
     * Erwartung:
     * - getCategories liefert genau so viele Eintraege wie Category-Enumwerte,
     *   in Enum-Reihenfolge, mit korrektem Label (MEAT -> "Fleisch").
     */
    @Test
    void getCategories_returnsAllCategoriesWithGermanLabels() {
        // Arrange: kein Setup noetig, Service ist zustandslos.

        // Act: nur die Categories abrufen.
        List<EnumOptionDto> categories = metadataService.getCategories();

        // Assert: Anzahl, Reihenfolge und Beispiel-Mapping pruefen.
        assertEquals(Category.values().length, categories.size());
        assertEquals(Category.values()[0].name(), categories.get(0).value());
        assertEquals("Fleisch", findLabel(categories, "MEAT"));
    }

    /**
     * Hilfsmethode: liefert das displayName-Label zu einem Rohwert.
     */
    private static String findLabel(List<EnumOptionDto> options, String value) {
        return options.stream()
                .filter(option -> option.value().equals(value))
                .map(EnumOptionDto::displayName)
                .findFirst()
                .orElseThrow();
    }
}
