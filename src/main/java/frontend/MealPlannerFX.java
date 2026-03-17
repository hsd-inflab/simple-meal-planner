package frontend;

import frontend.pages.AddGroceryPage;
import frontend.pages.AddMealPlanPage;
import frontend.pages.AddRecipePage;
import frontend.pages.AvailableRecipePage;
import frontend.pages.EditRecipePage;
import frontend.pages.GenerateRecipePage;
import frontend.pages.MainPage;
import frontend.pages.MealPlanPage;
import frontend.pages.Page;
import frontend.pages.PantryPage;
import frontend.pages.RecipePage;

import java.util.Locale;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import models.Route;
import services.MealPlannerService;
import services.RecipeAPIService;

/**
 * JavaFX Main class for Frontend
 */
public class MealPlannerFX extends Application {

    // Services
    private MealPlannerService mealPlanner;
    private RecipeAPIService recipeAPIService;

    // Frontend Controller
    private Navigator navigator;
    private AppState appState;

    private Locale locale = Locale.GERMAN;

    public MealPlannerFX() {
        this.mealPlanner = new MealPlannerService(); // Standard-Konstruktor
        this.recipeAPIService = new RecipeAPIService();
    }

    public MealPlannerFX(MealPlannerService mealPlanner) {
        this.mealPlanner = mealPlanner;
        this.recipeAPIService = new RecipeAPIService();
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Page.setLocale(locale);
        navigator = new Navigator(root);
        appState = new AppState();

        // register a route to create a connection between enum value and desired page
        navigator.register(Route.MAIN, new MainPage(navigator, primaryStage));
        navigator.register(Route.RECIPE, new RecipePage(navigator, mealPlanner, appState));
        navigator.register(Route.ADD_RECIPE, new AddRecipePage(navigator, mealPlanner));
        navigator.register(Route.EDIT_RECIPE, new EditRecipePage(navigator, mealPlanner, appState));
        navigator.register(Route.GENERATE_RECIPE, new GenerateRecipePage(navigator, mealPlanner, recipeAPIService));
        navigator.register(Route.PANTRY, new PantryPage(navigator, mealPlanner));
        navigator.register(Route.ADD_GROCERY, new AddGroceryPage(navigator, mealPlanner));
        navigator.register(Route.MEALPLAN, new MealPlanPage(navigator, mealPlanner));
        navigator.register(Route.AVAILABLE_RECIPES, new AvailableRecipePage(navigator, mealPlanner));
        navigator.register(Route.ADD_MEALPLAN, new AddMealPlanPage(navigator, mealPlanner));

        primaryStage.setTitle("HSD MealPlanner");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        navigator.show(Route.MAIN); // Show main Page
    }
}
