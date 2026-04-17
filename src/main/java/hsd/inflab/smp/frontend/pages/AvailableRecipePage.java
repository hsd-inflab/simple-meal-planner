package hsd.inflab.smp.frontend.pages;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.enums.Route;
import hsd.inflab.smp.frontend.NavigationButton;
import hsd.inflab.smp.frontend.Navigator;
import hsd.inflab.smp.service.MealPlannerService;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class AvailableRecipePage extends Page {
    MealPlannerService mealPlanner;
    private ListView<RecipeDto> availableRecipesListView;

    public AvailableRecipePage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Rezepte die du kochen kannst:");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        availableRecipesListView = new ListView<>();
        availableRecipesListView.setPrefHeight(400);

        Button backButton = new NavigationButton("Zurück zu Speisepläne", Route.MEALPLAN, navigator);

        root.getChildren().addAll(titleLabel, availableRecipesListView, backButton);
        return root;
    }

    private void refreshAvailableRecipes() {
        availableRecipesListView.getItems().clear();
        List<RecipeDto> available = mealPlanner.recipeService.getAvailableRecipes();
        availableRecipesListView.getItems().addAll(available);
    }

    @Override
    public void onShow() {
        refreshAvailableRecipes();
    }
}
