// Testklasse für RecipeApiMapper
// Ziel: Überprüft die Mapping-Logik von ExternalRecipeDto/ExternalCrawlRecipeDto zu RecipeResponseDto
// - Prüft verschiedene Fälle für Zutaten, Mengen, Einheiten und Beschreibung

package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.*;

import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeIngredientDto;
import hsd.inflab.smp.dto.response.RecipeIngredientResponseDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.mapper.RecipeApiMapper;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecipeApiMapperTest {

    private RecipeApiMapper recipeApiMapper;

    /**
     * Initialisiert vor jedem Test eine neue Instanz des RecipeApiMapper.
     */
    @BeforeEach
    void setUp() {
        recipeApiMapper = new RecipeApiMapper(); // Frische Instanz für jeden Test
    }

    /**
     * Testet das Mapping mit Such- und Crawl-Rezept: Titel, Beschreibung und Zutaten werden korrekt übernommen.
     */
    @Test
    void toRecipeDto_withSearchRecipeAndCrawlRecipe_mapsTitleDescriptionAndIngredients() {
        // Externe Zutat mit Komma als Dezimaltrennzeichen
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("2,5", "Mehl", "g");

        // Externes Rezept mit Titel und Zutaten
        ExternalRecipeDto searchRecipe = new ExternalRecipeDto(
                "Pfannkuchen", "https://example.com/pfannkuchen", "pfannkuchen", List.of(externalIngredient));

        // Crawl-Rezept mit mehreren Schritten
        ExternalCrawlRecipeDto crawlRecipe =
                new ExternalCrawlRecipeDto(List.of("Teig vorbereiten", "Pfannkuchen braten"));

        // Mapping durchführen
        RecipeResponseDto result = recipeApiMapper.toRecipeDto(searchRecipe, crawlRecipe);

        assertNull(result.id()); // ID ist nicht gesetzt
        assertEquals("Pfannkuchen", result.name()); // Titel übernommen
        assertEquals(
                "Teig vorbereiten\n\nPfannkuchen braten",
                result.description()); // Schritte werden mit Leerzeile verbunden

        assertEquals(1, result.ingredientsPerPerson().size()); // Eine Zutat erwartet

        RecipeIngredientResponseDto ingredient = result.ingredientsPerPerson().getFirst(); // Erste (und einzige) Zutat

        assertNotNull(ingredient.id()); // Zutat hat eine ID
        assertEquals("Mehl", ingredient.name()); // Name übernommen
        assertEquals(2.5, ingredient.amount()); // Menge korrekt als Double geparst
        assertNotNull(ingredient.unit()); // Einheit gesetzt
    }

    /**
     * Testet das Mapping ohne Crawl-Rezept: Beschreibung ist null, Zutaten und Titel werden übernommen.
     */
    @Test
    void toRecipeDto_withoutCrawlRecipe_mapsTitleAndIngredientsButDescriptionIsNull() {
        // Externe Zutat
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("1", "Apfel", "Stück");

        // Externes Rezept mit einer Zutat
        ExternalRecipeDto externalRecipe = new ExternalRecipeDto(
                "Apfelkuchen", "https://example.com/apfelkuchen", "apfelkuchen", List.of(externalIngredient));

        // Mapping durchführen (ohne CrawlRecipe)
        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertNull(result.id()); // ID ist nicht gesetzt
        assertEquals("Apfelkuchen", result.name()); // Titel übernommen
        assertNull(result.description()); // Keine Beschreibung, da kein CrawlRecipe

        assertEquals(1, result.ingredientsPerPerson().size()); // Eine Zutat erwartet
        assertEquals("Apfel", result.ingredientsPerPerson().getFirst().name()); // Name übernommen
        assertEquals(1.0, result.ingredientsPerPerson().getFirst().amount()); // Menge als Double
    }

    /**
     * Testet das Mapping, wenn die Zutatenliste null ist: Zutatenliste im Ergebnis ist leer.
     */
    @Test
    void toRecipeDto_withNullIngredients_returnsEmptyIngredientList() {
        // Externes Rezept ohne Zutaten
        ExternalRecipeDto externalRecipe = new ExternalRecipeDto(
                "Rezept ohne Zutaten", "https://example.com/rezept", "keywords", null // Zutatenliste ist null
                );

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertNotNull(result.ingredientsPerPerson()); // Zutatenliste existiert
        assertTrue(result.ingredientsPerPerson().isEmpty()); // ...ist aber leer
    }

    /**
     * Testet das Mapping, wenn die Schritt-Liste im CrawlRecipe null ist: Beschreibung ist null.
     */
    @Test
    void toRecipeDto_withNullSteps_setsDescriptionToNull() {
        // Externes Rezept
        ExternalRecipeDto searchRecipe =
                new ExternalRecipeDto("Suppe", "https://example.com/suppe", "suppe", List.of());

        // CrawlRecipe mit null-Schritten
        ExternalCrawlRecipeDto crawlRecipe = new ExternalCrawlRecipeDto(null);

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(searchRecipe, crawlRecipe);

        assertNull(result.description()); // Beschreibung ist null
    }

    /**
     * Testet das Mapping, wenn die Schritt-Liste leer ist: Beschreibung ist null.
     */
    @Test
    void toRecipeDto_withEmptySteps_setsDescriptionToNull() {
        // Externes Rezept
        ExternalRecipeDto searchRecipe =
                new ExternalRecipeDto("Suppe", "https://example.com/suppe", "suppe", List.of());

        // CrawlRecipe mit leerer Schritt-Liste
        ExternalCrawlRecipeDto crawlRecipe = new ExternalCrawlRecipeDto(List.of());

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(searchRecipe, crawlRecipe);

        assertNull(result.description()); // Beschreibung ist null
    }

    /**
     * Testet das Mapping, wenn mehrere Schritte vorhanden sind: Schritte werden mit Leerzeile verbunden.
     */
    @Test
    void toRecipeDto_withMultipleSteps_joinsStepsWithBlankLine() {
        // Externes Rezept
        ExternalRecipeDto searchRecipe =
                new ExternalRecipeDto("Suppe", "https://example.com/suppe", "suppe", List.of());

        // CrawlRecipe mit mehreren Schritten
        ExternalCrawlRecipeDto crawlRecipe = new ExternalCrawlRecipeDto(List.of("Schneiden", "Kochen", "Servieren"));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(searchRecipe, crawlRecipe);

        assertEquals("Schneiden\n\nKochen\n\nServieren", result.description()); // Schritte verbunden
    }

    /**
     * Testet das Mapping, wenn die Mengenangabe leer ist: Menge wird zu 0.0.
     */
    @Test
    void toRecipeDto_withBlankAmount_mapsAmountToZero() {
        // Externe Zutat mit leerer Mengenangabe
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("   ", "Salz", "g");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Salzrezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(0.0, result.ingredientsPerPerson().getFirst().amount()); // Menge ist 0.0
    }

    /**
     * Testet das Mapping, wenn die Mengenangabe null ist: Menge wird zu 0.0.
     */
    @Test
    void toRecipeDto_withNullAmount_mapsAmountToZero() {
        // Externe Zutat mit null-Menge
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto(null, "Salz", "g");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Salzrezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(0.0, result.ingredientsPerPerson().getFirst().amount()); // Menge ist 0.0
    }

    /**
     * Testet das Mapping, wenn die Mengenangabe ungültig ist: Menge wird zu 0.0.
     */
    @Test
    void toRecipeDto_withInvalidAmount_mapsAmountToZero() {
        // Externe Zutat mit ungültiger Mengenangabe
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("abc", "Salz", "g");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Salzrezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(0.0, result.ingredientsPerPerson().getFirst().amount()); // Menge ist 0.0
    }

    /**
     * Testet das Mapping, wenn die Mengenangabe ein Komma als Dezimaltrennzeichen hat: Wert wird korrekt geparst.
     */
    @Test
    void toRecipeDto_withDecimalCommaAmount_mapsAmountCorrectly() {
        // Externe Zutat mit Komma-Dezimaltrennzeichen
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("2,75", "Milch", "ml");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Milchrezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(2.75, result.ingredientsPerPerson().getFirst().amount()); // Wert korrekt geparst
    }

    /**
     * Testet das Mapping, wenn die Mengenangabe einen Punkt als Dezimaltrennzeichen hat: Wert wird korrekt geparst.
     */
    @Test
    void toRecipeDto_withDecimalPointAmount_mapsAmountCorrectly() {
        // Externe Zutat mit Punkt-Dezimaltrennzeichen
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("2.75", "Milch", "ml");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Milchrezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(2.75, result.ingredientsPerPerson().getFirst().amount()); // Wert korrekt geparst
    }

    /**
     * Testet das Mapping, wenn die Einheit null ist: Einheit wird zu Unit.NONE.
     */
    @Test
    void toRecipeDto_withNullUnit_mapsUnitToNone() {
        // Externe Zutat mit null-Einheit
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("1", "Zutat", null);

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Rezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(Unit.NONE, result.ingredientsPerPerson().getFirst().unit()); // Einheit ist NONE
    }

    /**
     * Testet das Mapping, wenn die Einheit leer ist: Einheit wird zu Unit.NONE.
     */
    @Test
    void toRecipeDto_withBlankUnit_mapsUnitToNone() {
        // Externe Zutat mit leerer Einheit
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("1", "Zutat", "   ");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Rezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(Unit.NONE, result.ingredientsPerPerson().getFirst().unit()); // Einheit ist NONE
    }

    /**
     * Testet das Mapping, wenn die Einheit unbekannt ist: Einheit wird zu Unit.NONE.
     */
    @Test
    void toRecipeDto_withUnknownUnit_mapsUnitToNone() {
        // Externe Zutat mit unbekannter Einheit
        ExternalRecipeIngredientDto externalIngredient =
                new ExternalRecipeIngredientDto("1", "Zutat", "unbekannteEinheit");

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Rezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(Unit.NONE, result.ingredientsPerPerson().getFirst().unit()); // Einheit ist NONE
    }

    /**
     * Testet das Mapping, wenn die Einheit bekannt ist: Einheit wird korrekt gemappt.
     */
    @Test
    void toRecipeDto_withKnownUnit_mapsUnitFromApiLookupMap() {
        // Hole eine bekannte API-Einheit aus der Lookup-Map
        String knownApiUnit = Unit.getApiLookupMap(java.util.Locale.GERMAN).entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != Unit.NONE)
                .findFirst()
                .orElseThrow()
                .getKey();

        Unit expectedUnit = Unit.getApiLookupMap(java.util.Locale.GERMAN).get(knownApiUnit);

        // Externe Zutat mit bekannter Einheit
        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("1", "Zutat", knownApiUnit);

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Rezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(expectedUnit, result.ingredientsPerPerson().getFirst().unit()); // Einheit korrekt gemappt
    }

    /**
     * Testet das Mapping, wenn die Einheit Leerzeichen und Großbuchstaben enthält: Einheit wird korrekt erkannt.
     */
    @Test
    void toRecipeDto_withKnownUnitHavingSpacesAndDifferentCase_mapsUnitCorrectly() {
        // Hole eine bekannte API-Einheit aus der Lookup-Map
        String knownApiUnit = Unit.getApiLookupMap(Locale.GERMAN).entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != Unit.NONE)
                .findFirst()
                .orElseThrow()
                .getKey();

        Unit expectedUnit = Unit.getApiLookupMap(Locale.GERMAN).get(knownApiUnit);

        // Modifiziere die Einheit (Großbuchstaben, Leerzeichen)
        String modifiedApiUnit = "  " + knownApiUnit.toUpperCase() + "  ";

        ExternalRecipeIngredientDto externalIngredient = new ExternalRecipeIngredientDto("1", "Zutat", modifiedApiUnit);

        ExternalRecipeDto externalRecipe =
                new ExternalRecipeDto("Rezept", "source", "keywords", List.of(externalIngredient));

        RecipeResponseDto result = recipeApiMapper.toRecipeDto(externalRecipe);

        assertEquals(expectedUnit, result.ingredientsPerPerson().getFirst().unit()); // Einheit korrekt erkannt
    }
}
