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

    public List<PantryItem> loadPantry() {
        return loadFromFile("pantry.json", new TypeReference<>() {}, new ArrayList<>());
    }

    public void savePantry(List<PantryItem> pantry) {
        saveToFile("pantry.json", pantry);
    }

    public List<Recipe> loadRecipeBook() {
        return loadFromFile("recipebook.json", new TypeReference<>() {}, new ArrayList<>());
    }

    public void saveRecipeBook(List<Recipe> recipeBook) {
        saveToFile("recipebook.json", recipeBook);
    }

    public Map<LocalDate, DailyMeal> loadMealPlans() {
        return loadFromFile("mealplans.json", new TypeReference<>() {}, new HashMap<>());
    }

    public void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {
        saveToFile("mealplans.json", dailyMealPlans);
        System.out.println("mealplans.json wurde gespeichert.");
    }
}
