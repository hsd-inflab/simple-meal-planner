package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * main service, provides access and stores all objects/lists used at runtime
 */
@Service
public class MealPlannerService {

    private List<Recipe> recipeBook;
    List<PantryItem> pantry;
    private Map<LocalDate, DailyMeal> dailyMealPlans;

    private final DataService dataService;
    private final ConfigService configService;
    private final PasswordService passwordService;
    public final PantryService pantryService;

    public MealPlannerService(
            DataService dataService,
            ConfigService configService,
            PasswordService passwordService,
            PantryService pantryService) {
        this.dataService = dataService;
        this.configService = configService;
        this.passwordService = passwordService;
        this.pantryService = pantryService;
    }

    @PostConstruct
    public void init() {
        loadData();
        saveDataAfterTermination();
    }

    public boolean verifyAPIPassword(String password) {
        return passwordService.verifyPassword(password, configService.getRecipeApiPasswordhash());
    }

    public List<Recipe> getRecipeBook() {
        return recipeBook;
    }

    public Map<LocalDate, DailyMeal> getDailyMealPlans() {
        return dailyMealPlans;
    }

    public void saveRecipeBook() {
        dataService.saveRecipeBook(recipeBook);
    }

    private void loadData() {
        recipeBook = dataService.loadRecipeBook();
        pantry = dataService.loadPantry();
        dailyMealPlans = dataService.loadMealPlans();
    }

    private void saveDataAfterTermination() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dataService.savePantry(pantry);
            dataService.saveRecipeBook(recipeBook);
            dataService.saveMealPlans(dailyMealPlans);
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
}
