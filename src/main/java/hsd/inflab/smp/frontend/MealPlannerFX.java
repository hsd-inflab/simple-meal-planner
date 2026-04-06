package hsd.inflab.smp.frontend;

import hsd.inflab.smp.frontend.pages.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import hsd.inflab.smp.model.Route;
import org.springframework.context.ConfigurableApplicationContext;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.service.RecipeAPIService;

import java.util.Locale;

/**
 * JavaFX Main class for Frontend
 */
public class MealPlannerFX extends Application {

    private MealPlannerService mealPlanner;
    private RecipeAPIService recipeAPIService;

    private final Locale locale = Locale.GERMAN;

    /**
     * Wichtig: Für den Zugriff auf die Beans (von Spring erstellte Instanzen der Klasse mit '@Service' annotiert) muss
     * der ApplicationContext von Spring übergeben werden, da JavaFX die Instanziierung der Main-Klasse übernimmt.
     */

    private static ConfigurableApplicationContext context;
    public static void setApplicationContext(ConfigurableApplicationContext ctx) {
        context = ctx;
    }

    @Override
    public void start(Stage primaryStage) {
        this.mealPlanner = context.getBean(MealPlannerService.class);
        this.recipeAPIService = context.getBean(RecipeAPIService.class);

        BorderPane root = new BorderPane();
        Page.setLocale(locale);
        Navigator navigator = new Navigator(root);
        AppState appState = new AppState();

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

        navigator.show(Route.MAIN);
    }
}