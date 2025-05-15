package services;

import com.fasterxml.jackson.databind.ObjectMapper;

import models.DailyMeal;
import models.Ingredient;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.time.LocalDate;

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<PantryItem> pantry;
    private Map<LocalDate, DailyMeal> dailyMealPlans = new TreeMap<>();
    
    public MealPlannerService() {
        recipeBook = new ArrayList<>();
        pantry = new ArrayList<>();

        loadPantry();
        loadRecipeBook();
        loadMealPlans();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {     //this thread runs after the program terminates
            savePantry();
            saveRecipeBook();
            saveMealPlans();
            System.out.println("mealplanner gracefully terminated.");
        }));
    }

    public List<Recipe> getRecipeBook() {
        return recipeBook;
    }

    public List<PantryItem> getPantry() {
        return pantry;
    }
    public Map<LocalDate, DailyMeal> getDailyMealPlans() {
        return dailyMealPlans;
    }
    
    private void loadMealPlans() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'loadMealPlans'");
    }

    private void loadPantry() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pantry = objectMapper.readValue(
                    new File("pantry.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PantryItem.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading pantry: " + e.getMessage());
            pantry = new ArrayList<>();
        }
    }

    private void loadRecipeBook() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            recipeBook = objectMapper.readValue(
                    new File("recipebook.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Recipe.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading recipe book: " + e.getMessage());
            recipeBook = new ArrayList<>();
        }
    }
    //save pantry and recipe book to json files
    private void savePantry() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("pantry.json"), pantry);
            System.out.println("Pantry wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Pantry: " + e.getMessage());
        }
    }

    private void saveRecipeBook() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("recipebook.json"), recipeBook);
            System.out.println("Rezeptbuch wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern des Rezeptbuchs: " + e.getMessage());
        }
    }

    private void saveMealPlans() {
        // insert jackson wrapper method
        System.out.println("meal plans saved.");
    }

    

    

}
