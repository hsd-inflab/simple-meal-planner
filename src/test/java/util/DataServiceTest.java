package util;

import com.fasterxml.jackson.core.type.TypeReference;
import models.DailyMeal;
import models.PantryItem;
import models.Recipe;
import org.junit.jupiter.api.*;
import services.ConfigService;
import services.DataService;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DataServiceTest {

    private DataService dataService;

    // Dateinamen werden jetzt aus config.properties geladen
    private static final String TEMP_FILE = ConfigService.get("temp.file");
    private static final String PANTRY_FILE = ConfigService.get("pantry.file");
    private static final String RECIPE_BOOK_FILE = ConfigService.get("recipebook.file");
    private static final String MEAL_PLANS_FILE = ConfigService.get("mealplans.file");

    @BeforeEach
    void setUp() {
        dataService = new DataService();
    }

    @AfterEach
    void cleanUp() {
        File file = new File(TEMP_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void loadFromFile_shouldReturnDefaultValueOnMissingFile() {
        List<PantryItem> result = dataService.loadFromFile("nonexistent.json", new TypeReference<>() {}, List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveToFile_and_loadFromFile_shouldWorkCorrectly() {
        List<String> original = List.of("apple", "banana", "cherry");
        dataService.saveToFile(TEMP_FILE, original);

        List<String> loaded = dataService.loadFromFile(TEMP_FILE, new TypeReference<>() {}, List.of());
        assertEquals(original, loaded);
    }

    @Test
    void loadPantry_shouldReturnList() {
        List<PantryItem> result = dataService.loadPantry(PANTRY_FILE);
        assertNotNull(result);
        assertTrue(new File(PANTRY_FILE).exists(), "Die Datei " + PANTRY_FILE + " sollte existieren.");
    }

    @Test
    void loadRecipeBook_shouldReturnList() {
        List<Recipe> result = dataService.loadRecipeBook(RECIPE_BOOK_FILE);
        assertNotNull(result);
        assertTrue(new File(RECIPE_BOOK_FILE).exists(), "Die Datei " + RECIPE_BOOK_FILE + " sollte existieren.");
    }

    @Test
    void loadMealPlans_shouldReturnMap() {
        Map<LocalDate, DailyMeal> result = dataService.loadMealPlans(MEAL_PLANS_FILE);
        assertNotNull(result);
        assertTrue(new File(MEAL_PLANS_FILE).exists(), "Die Datei " + MEAL_PLANS_FILE + " sollte existieren.");
    }
}
