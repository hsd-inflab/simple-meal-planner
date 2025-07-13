package services;

import models.DailyMeal;
import models.PantryItem;
import models.Recipe;
import java.util.*;
import java.time.LocalDate;

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<PantryItem> pantry;
    private Map<LocalDate, DailyMeal> dailyMealPlans;

    private static final String PANTRY_FILE = "pantry.json";
    private static final String RECIPE_BOOK_FILE = "recipebook.json";
    private static final String MEAL_PLANS_FILE = "mealplans.json";

    private DataService dataService;
    
    public MealPlannerService() {
        dataService = new DataService();

        loadData();
        saveDataAfterTermination();
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
    
    private void loadData() {
        recipeBook = dataService.loadRecipeBook(RECIPE_BOOK_FILE);
        pantry = dataService.loadPantry(PANTRY_FILE);
        dailyMealPlans = dataService.loadMealPlans(MEAL_PLANS_FILE);          //TODO: implement meal plans in loader class.
        dataService.loadMealPlans(MEAL_PLANS_FILE);
    }
    
    private void saveDataAfterTermination() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {     //this thread runs after the program terminates
            dataService.savePantry(PANTRY_FILE, pantry);
            dataService.saveRecipeBook(RECIPE_BOOK_FILE, recipeBook);
            dataService.saveMealPlans(MEAL_PLANS_FILE, dailyMealPlans);
            System.out.println("mealplanner gracefully terminated.");
        }));
    }

}
