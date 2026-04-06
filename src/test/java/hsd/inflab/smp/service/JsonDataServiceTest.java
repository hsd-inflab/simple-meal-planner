package hsd.inflab.smp.service;

import hsd.inflab.smp.model.DailyMeal;
import hsd.inflab.smp.model.PantryItem;
import hsd.inflab.smp.model.Recipe;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsonDataServiceTest {

    @Autowired
    private JsonDataService dataService;

    @Autowired
    private ConfigService configService;

    @AfterEach
    void cleanUp() {
        File file = new File(configService.getTempFile());
        if (file.exists()) {
            assertTrue(file.delete(), "Temp-Datei konnte nicht gelöscht werden.");
        }
    }

    @Test
    void loadFromFile_shouldReturnDefaultValueOnMissingFile() {
        List<PantryItem> result =
                dataService.loadFromFile("nonexistent.json", new TypeReference<>() {}, List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveToFile_and_loadFromFile_shouldWorkCorrectly() {
        String tempFile = configService.getTempFile();
        List<String> original = List.of("apple", "banana", "cherry");

        dataService.saveToFile(tempFile, original);
        List<String> loaded = dataService.loadFromFile(tempFile, new TypeReference<>() {}, List.of());

        assertEquals(original, loaded);
    }

    @Test
    void loadPantry_shouldReturnList() {
        List<PantryItem> result = dataService.loadPantry();

        assertNotNull(result);
        assertTrue(new File(configService.getPantryFile()).exists(),
                "Die Datei " + configService.getPantryFile() + " sollte existieren.");
    }

    @Test
    void loadRecipeBook_shouldReturnList() {
        List<Recipe> result = dataService.loadRecipeBook();

        assertNotNull(result);
        assertTrue(new File(configService.getRecipebookFile()).exists(),
                "Die Datei " + configService.getRecipebookFile() + " sollte existieren.");
    }

    @Test
    void loadMealPlans_shouldReturnMap() {
        Map<LocalDate, DailyMeal> result = dataService.loadMealPlans();

        assertNotNull(result);
        assertTrue(new File(configService.getMealplansFile()).exists(),
                "Die Datei " + configService.getMealplansFile() + " sollte existieren.");
    }
}