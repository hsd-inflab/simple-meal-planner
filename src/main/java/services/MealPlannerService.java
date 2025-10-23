package services;

import models.DailyMeal;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

import java.util.*;
import java.time.LocalDate;

/**
 * main service, provides access and stores all objects/lists used at runtime
 */

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<PantryItem> pantry;
    private Map<LocalDate, DailyMeal> dailyMealPlans;

    private static final String PANTRY_FILE = ConfigService.get("pantry.file");
    private static final String RECIPE_BOOK_FILE = ConfigService.get("recipebook.file");
    private static final String MEAL_PLANS_FILE = ConfigService.get("mealplans.file");
    private static final String RECIPE_API_PW_HASH = ConfigService.get("recipe.api.passwordhash");

    private DataService dataService;
    
    public MealPlannerService() {
        dataService = new DataService();
        loadData();
        saveDataAfterTermination();
    }

    public boolean verifyAPIPassword(String password) {
        PasswordService pwService = new PasswordService();
        return pwService.verifyPassword(password, RECIPE_API_PW_HASH);
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

    public void saveRecipeBook() {
        dataService.saveRecipeBook(RECIPE_BOOK_FILE, recipeBook);
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

    public List<Recipe> getAvailableRecipes() {
        List<Recipe> availableRecipes = new ArrayList<>();

        for (Recipe recipe : recipeBook) {
            boolean canMake = true;

            // Durchlaufe alle Zutaten des Rezepts
            for (RecipeIngredient recipeIng : recipe.getIngredients()) {
                // Suche nach der Zutat in der Pantry
                Optional<PantryItem> matchingItem = pantry.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(recipeIng.getName())) // Vergleiche die Namen der
                        // Zutaten
                        .findFirst(); // Finde das erste PantryItem, das der Zutat entspricht

                // Prüfe, ob die Zutat in der Pantry vorhanden ist und ob die Menge ausreicht
                if (matchingItem.isEmpty() || matchingItem.get().getAmount() < recipeIng.getAmount()) {
                    canMake = false; // Rezept kann nicht gemacht werden, da Zutat fehlt oder Menge nicht ausreicht
                    break; // Schleife abbrechen, da es nicht mehr möglich ist, das Rezept zu machen
                }
            }

            // Wenn das Rezept mit den Zutaten zubereitet werden kann, füge es zur Liste der
            // verfügbaren Rezepte hinzu
            if (canMake) {
                availableRecipes.add(recipe);
            }
        }

        return availableRecipes;
    }


    // // Persist the current recipe book immediately (not only on shutdown)
    // public void saveRecipeBookNow() {
    //     dataService.saveRecipeBook(recipeBook);
    // }

}
