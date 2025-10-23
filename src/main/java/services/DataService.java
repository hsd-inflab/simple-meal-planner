package services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.DailyMeal;
import models.PantryItem;
import models.Recipe;

/**
 * provides data persistence
 */

public class DataService {

    private final ObjectMapper objectMapper;

    public DataService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

    public List<PantryItem> loadPantry(String filename) {
        return loadFromFile(filename, new TypeReference<>() {}, new ArrayList<>());
    }

    public void savePantry(String filename, List<PantryItem> pantry) {
        saveToFile(filename, pantry);
    }

    public List<Recipe> loadRecipeBook(String filename) {
        return loadFromFile(filename, new TypeReference<>() {}, new ArrayList<>());
    }

    public void saveRecipeBook(String filename, List<Recipe> recipeBook) {
        saveToFile(filename, recipeBook);
    }

    public Map<LocalDate, DailyMeal> loadMealPlans(String filename) {
        return loadFromFile(filename, new TypeReference<>() {}, new HashMap<>());
    }

    public void saveMealPlans(String filename, Map<LocalDate, DailyMeal> dailyMealPlans) {
        saveToFile(filename, dailyMealPlans);
    }
}
