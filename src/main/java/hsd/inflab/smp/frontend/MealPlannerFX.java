package hsd.inflab.smp.frontend;

import hsd.inflab.smp.frontend.pages.AddGroceryPage;
import hsd.inflab.smp.frontend.pages.AddMealPlanPage;
import hsd.inflab.smp.frontend.pages.AddRecipePage;
import hsd.inflab.smp.frontend.pages.AvailableRecipePage;
import hsd.inflab.smp.frontend.pages.EditRecipePage;
import hsd.inflab.smp.frontend.pages.GenerateRecipePage;
import hsd.inflab.smp.frontend.pages.MainPage;
import hsd.inflab.smp.frontend.pages.MealPlanPage;
import hsd.inflab.smp.frontend.pages.Page;
import hsd.inflab.smp.frontend.pages.PantryPage;
import hsd.inflab.smp.frontend.pages.RecipePage;
import hsd.inflab.smp.model.Route;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.service.RecipeAPIService;
import java.util.Locale;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX Main class for Frontend
 */
public class MealPlannerFX extends Application {

    private static final Locale LOCALE = Locale.GERMAN;

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
        MealPlannerService mealPlanner = context.getBean(MealPlannerService.class);
        RecipeAPIService recipeAPIService = context.getBean(RecipeAPIService.class);

        BorderPane root = new BorderPane();
        Page.setLocale(LOCALE);
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
