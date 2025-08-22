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
        recipeBook = dataService.loadRecipeBook();
        pantry = dataService.loadPantry();
        dailyMealPlans = new TreeMap<>();           //TODO: implement meal plans in loader class.
        dataService.loadMealPlans();
    }
    
    private void saveDataAfterTermination() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {     //this thread runs after the program terminates
            dataService.savePantry(pantry);
            dataService.saveRecipeBook(recipeBook);
            dataService.saveMealPlans(dailyMealPlans);
            System.out.println("mealplanner gracefully terminated.");
        }));
    }

    // Persist the current recipe book immediately (not only on shutdown)
    public void saveRecipeBookNow() {
        dataService.saveRecipeBook(recipeBook);
    }

}
