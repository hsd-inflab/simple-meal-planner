package services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import models.DailyMeal;
import models.PantryItem;
import models.Recipe;

//TODO: there is a lot of code duplication in this class. a refactoring is needed.

public class DataService {
    public void loadMealPlans() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'loadMealPlans'");
    }

    public List<PantryItem> loadPantry() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<PantryItem> pantry;
        try {
            pantry = objectMapper.readValue(
                    new File("pantry.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PantryItem.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading pantry: " + e.getMessage());
            pantry = new ArrayList<>();
        }
        return pantry;
    }

    public List<Recipe> loadRecipeBook() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Recipe> recipeBook;
        try {
            recipeBook = objectMapper.readValue(
                    new File("recipebook.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Recipe.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading recipe book: " + e.getMessage());
            recipeBook = new ArrayList<>();
        }
        return recipeBook;
    }
    //save pantry and recipe book to json files
    public void savePantry(List<PantryItem> pantry) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("pantry.json"), pantry);
            System.out.println("Pantry wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Pantry: " + e.getMessage());
        }
    }

    public void saveRecipeBook(List<Recipe> recipeBook) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("recipebook.json"), recipeBook);
            System.out.println("Rezeptbuch wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern des Rezeptbuchs: " + e.getMessage());
        }
    }

    public void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {
        // insert jackson wrapper method
        System.out.println("meal plans saved.");
    }
}
