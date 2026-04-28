package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit-Tests für {@link DailyMealService}.
 *
 * Diese Testklasse prüft die Logik des Services isoliert mit Mockito:
 * - Repository-Aufrufe werden gemockt
 * - Rezept-Konvertierung wird gemockt
 * - Es wird geprüft, ob Insert, Update, Fehlerfälle und DTO-Konvertierung korrekt funktionieren
 */
@ExtendWith(MockitoExtension.class)
class DailyMealServiceTest {

    // Mock für den Zugriff auf DailyMeal-Daten aus der Datenbank
    @Mock
    private DailyMealRepository dailyMealRepo;

    // Mock für den Zugriff auf Rezepte aus der Datenbank
    @Mock
    private RecipeRepository recipeRepo;

    // Mock für die Umwandlung von Recipe-Entity -> RecipeDto
    @Mock
    private RecipeService recipeService;

    // Der echte Service wird mit den Mocks "gefüllt"
    @InjectMocks
    private DailyMealService dailyMealService;

    /**
     * Prüft den Insert-Fall:
     * Wenn für das Datum noch kein DailyMeal existiert, soll ein neuer Eintrag gespeichert werden.
     */
    @Test
    void saveOrUpdateDailyMeal_inserts_whenDateNotExists() {
        // Testdatum für den neuen Meal-Plan
        LocalDate date = LocalDate.of(2026, 4, 21);

        // Frühstücksrezept mit zufälliger UUID
        UUID breakfastId = UUID.randomUUID();
        RecipeDto breakfastDto = new RecipeDto(breakfastId, "Omelett", "", null);

        // Eingabe-DTO: Frühstück ist gesetzt, Mittag/Abend nicht
        DailyMealDto input = new DailyMealDto(null, date, breakfastDto, null, null, 2, 0, 0);

        // Simuliertes Rezept-Entity aus der Datenbank
        Recipe breakfastEntity = new Recipe();

        // Für dieses Datum existiert noch kein Meal-Plan
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        // Das Rezept mit der UUID wird gefunden
        when(recipeRepo.findById(breakfastId)).thenReturn(Optional.of(breakfastEntity));

        // Beim Speichern soll das übergebene Entity direkt zurückgegeben werden
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Das Entity soll wieder in das erwartete DTO umgewandelt werden
        when(recipeService.convertToDto(breakfastEntity)).thenReturn(breakfastDto);

        // Service-Methode ausführen
        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        // Prüfen, ob die Daten korrekt übernommen wurden
        assertEquals(date, result.date());
        assertEquals(2, result.breakfastServings());
        assertEquals(breakfastId, result.breakfastRecipe().id());

        // Sicherstellen, dass gespeichert wurde
        verify(dailyMealRepo).save(any(DailyMeal.class));
    }

    /**
     * Prüft den Update-Fall:
     * Wenn bereits ein DailyMeal für das Datum existiert, sollen die Werte überschrieben werden.
     */
    @Test
    void saveOrUpdateDailyMeal_updates_whenDateExists() {
        // Datum, für das bereits ein Eintrag vorhanden ist
        LocalDate date = LocalDate.of(2026, 4, 22);

        // Vorhandenes Entity aus der Datenbank
        DailyMeal existing = new DailyMeal();
        existing.setMealDate(date);

        // Neues DTO mit geänderten Portionszahlen
        DailyMealDto input = new DailyMealDto(null, date, null, null, null, 1, 2, 3);

        // Für das Datum existiert bereits ein Eintrag
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.of(existing));

        // Beim Speichern wird das vorhandene Entity zurückgegeben
        when(dailyMealRepo.save(existing)).thenReturn(existing);

        // Service aufrufen
        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        // Prüfen, ob die Portionszahlen korrekt übernommen wurden
        assertEquals(1, result.breakfastServings());
        assertEquals(2, result.lunchServings());
        assertEquals(3, result.dinnerServings());

        // Sicherstellen, dass genau dieses Entity gespeichert wurde
        verify(dailyMealRepo).save(existing);
    }

    /**
     * Prüft den Fall, dass im DTO keine Rezepte gesetzt sind.
     * Der Service soll dann keine Rezeptsuche durchführen und alle Rezeptfelder auf null lassen.
     */
    @Test
    void saveOrUpdateDailyMeal_setsNullRecipes_whenDtoRecipesNull() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 23);

        // DTO ohne Rezepte
        DailyMealDto input = new DailyMealDto(null, date, null, null, null, 1, 1, 1);

        // Für das Datum existiert noch kein Eintrag
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        // Beim Speichern wird das übergebene Entity direkt zurückgegeben
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Service aufrufen
        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        // Alle Rezeptfelder sollen null sein
        assertNull(result.breakfastRecipe());
        assertNull(result.lunchRecipe());
        assertNull(result.dinnerRecipe());

        // Es darf keine Rezeptsuche geben, weil im DTO keine Rezepte gesetzt waren
        verify(recipeRepo, never()).findById(any(UUID.class));
    }

    /**
     * Prüft den Fehlerfall:
     * Wenn eine Rezept-ID angegeben ist, das Rezept aber nicht existiert,
     * soll eine IllegalArgumentException geworfen werden.
     */
    @Test
    void saveOrUpdateDailyMeal_throws_whenRecipeMissing() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 24);

        // Zufällige Rezept-ID, die nicht gefunden wird
        UUID missingId = UUID.randomUUID();
        RecipeDto missingDto = new RecipeDto(missingId, "Unbekannt", "", null);

        // DTO mit einem fehlenden Frühstücksrezept
        DailyMealDto input = new DailyMealDto(null, date, missingDto, null, null, 1, 0, 0);

        // Für das Datum existiert noch kein Eintrag
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        // Die Rezeptsuche liefert nichts zurück
        when(recipeRepo.findById(missingId)).thenReturn(Optional.empty());

        // Erwartet wird eine IllegalArgumentException
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> dailyMealService.saveOrUpdateDailyMeal(input));

        // Fehlermeldung soll den erwarteten Hinweis enthalten
        assertTrue(ex.getMessage().contains("Rezept nicht gefunden"));

        // Bei Fehler darf nicht gespeichert werden
        verify(dailyMealRepo, never()).save(any(DailyMeal.class));
    }

    /**
     * Prüft den Sonderfall:
     * Wenn ein RecipeDto vorhanden ist, aber keine ID besitzt,
     * soll das Rezept als null behandelt werden.
     */
    @Test
    void saveOrUpdateDailyMeal_setsNullRecipe_whenRecipeIdNull() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 25);

        // Rezept-DTO ohne ID
        RecipeDto dtoWithoutId = new RecipeDto(null, "Ohne ID", "", null);

        // DTO enthält ein Frühstücksrezept ohne ID
        DailyMealDto input = new DailyMealDto(null, date, dtoWithoutId, null, null, 1, 0, 0);

        // Für das Datum existiert noch kein Eintrag
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        // Beim Speichern wird das übergebene Entity direkt zurückgegeben
        when(dailyMealRepo.save(any(DailyMeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Service aufrufen
        DailyMealDto result = dailyMealService.saveOrUpdateDailyMeal(input);

        // Weil keine ID vorhanden ist, muss das Rezept null bleiben
        assertNull(result.breakfastRecipe());

        // Es darf keine Rezeptsuche stattfinden
        verify(recipeRepo, never()).findById(any(UUID.class));
    }

    /**
     * Prüft die Umwandlung von Entity zu DTO:
     * Nur vorhandene Rezepte sollen mit recipeService.convertToDto() konvertiert werden.
     */
    @Test
    void getMealPlanByDate_convertsOnlyExistingRecipes() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 26);

        // Zwei Rezept-Entities, die im Meal-Plan referenziert werden
        Recipe breakfast = new Recipe();
        Recipe dinner = new Recipe();

        // Erwartete DTOs für die beiden Rezepte
        RecipeDto breakfastDto = new RecipeDto(UUID.randomUUID(), "Fruehstueck", "", null);
        RecipeDto dinnerDto = new RecipeDto(UUID.randomUUID(), "Abendessen", "", null);

        // DailyMeal-Entity mit Frühstück und Abendessen, aber ohne Mittagessen
        DailyMeal entity = new DailyMeal();
        entity.setMealDate(date);
        entity.setBreakfastRecipe(breakfast);
        entity.setLunchRecipe(null);
        entity.setDinnerRecipe(dinner);
        entity.setBreakfastServings(2);
        entity.setLunchServings(0);
        entity.setDinnerServings(3);

        // Repository liefert das gespeicherte Entity
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.of(entity));

        // Konvertierung der vorhandenen Rezepte wird gemockt
        when(recipeService.convertToDto(breakfast)).thenReturn(breakfastDto);
        when(recipeService.convertToDto(dinner)).thenReturn(dinnerDto);

        // Service aufrufen
        DailyMealDto result = dailyMealService.getMealPlanByDate(date);

        // Prüfen, ob Datum und Rezeptzuordnung korrekt sind
        assertEquals(date, result.date());
        assertEquals(breakfastDto, result.breakfastRecipe());
        assertNull(result.lunchRecipe());
        assertEquals(dinnerDto, result.dinnerRecipe());

        // Genau zwei Rezept-Konvertierungen sollen erfolgt sein
        verify(recipeService, times(2)).convertToDto(any(Recipe.class));
    }

    /**
     * Prüft den "nicht gefunden"-Fall:
     * Wenn für das Datum kein Mealplan existiert, soll null zurückgegeben werden.
     */
    @Test
    void getMealPlanByDate_returnsNull_whenNotFound() {
        // Testdatum
        LocalDate date = LocalDate.of(2026, 4, 27);

        // Repository findet keinen Datensatz
        when(dailyMealRepo.findByMealDate(date)).thenReturn(Optional.empty());

        // Service aufrufen
        DailyMealDto result = dailyMealService.getMealPlanByDate(date);

        // Erwartet: null, weil kein Mealplan existiert
        assertNull(result);
    }

    /**
     * Prüft die Zeitraum-Abfrage:
     * Die zurückgegebenen Mealplans sollen in einer Map mit LocalDate als Schlüssel landen.
     */
    @Test
    void getMealPlansMapBetween_returnsDateKeyedMap() {
        // Zeitraum definieren
        LocalDate start = LocalDate.of(2026, 4, 28);
        LocalDate end = LocalDate.of(2026, 4, 29);

        // Erster Tagesplan
        DailyMeal dayOne = new DailyMeal();
        dayOne.setMealDate(start);
        dayOne.setBreakfastServings(1);
        dayOne.setLunchServings(1);
        dayOne.setDinnerServings(1);

        // Zweiter Tagesplan
        DailyMeal dayTwo = new DailyMeal();
        dayTwo.setMealDate(end);
        dayTwo.setBreakfastServings(2);
        dayTwo.setLunchServings(2);
        dayTwo.setDinnerServings(2);

        // Repository liefert beide Mealplans im Zeitraum
        when(dailyMealRepo.findByMealDateBetween(start, end)).thenReturn(List.of(dayOne, dayTwo));

        // Service aufrufen
        Map<LocalDate, DailyMealDto> result = dailyMealService.getMealPlansMapBetween(start, end);

        // Map muss beide Tage enthalten
        assertEquals(2, result.size());

        // Der Starttag muss korrekt gemappt sein
        assertEquals(1, result.get(start).breakfastServings());

        // Der Endtag muss korrekt gemappt sein
        assertEquals(2, result.get(end).dinnerServings());
    }
}
