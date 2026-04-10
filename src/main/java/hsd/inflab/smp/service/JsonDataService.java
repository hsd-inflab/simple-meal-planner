package hsd.inflab.smp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hsd.inflab.smp.model.DailyMeal;
import hsd.inflab.smp.model.PantryItem;
import hsd.inflab.smp.model.Recipe;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * provides data persistence
 */
@Service("jsonDataService")
@Primary
public class JsonDataService implements DataService {

    private final ObjectMapper objectMapper;
    private final ConfigService configService;

    public JsonDataService(ConfigService configService) {
        this.configService = configService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public <T> T loadFromFile(String filename, TypeReference<T> typeReference, T defaultValue) {
        try {
            return objectMapper.readValue(new File(filename), typeReference);
        } catch (IOException e) {
            System.out.println("Fehler beim Laden von " + filename + ": " + e.getMessage());
            return defaultValue;
        }
    }

    public <T> void saveToFile(String filename, T data) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);
            System.out.println(filename + " wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern von " + filename + ": " + e.getMessage());
        }
    }

    @Override
    public List<PantryItem> loadPantry() {
        return loadFromFile(configService.getPantryFile(), new TypeReference<>() {}, new ArrayList<>());
    }

    @Override
    public void savePantry(List<PantryItem> pantry) {
        saveToFile(configService.getPantryFile(), pantry);
    }

    @Override
    public List<Recipe> loadRecipeBook() {
        return loadFromFile(configService.getRecipebookFile(), new TypeReference<>() {}, new ArrayList<>());
    }

    @Override
    public void saveRecipeBook(List<Recipe> recipeBook) {
        saveToFile(configService.getRecipebookFile(), recipeBook);
    }

    @Override
    public Map<LocalDate, DailyMeal> loadMealPlans() {
        return loadFromFile(configService.getMealplansFile(), new TypeReference<>() {}, new HashMap<>());
    }

    @Override
    public void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {
        saveToFile(configService.getMealplansFile(), dailyMealPlans);
    }
}
