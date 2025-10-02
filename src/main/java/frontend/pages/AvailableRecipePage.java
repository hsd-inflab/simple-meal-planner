package frontend.pages;

import frontend.NavigationButton;
import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import models.Recipe;
import models.Route;

import services.MealPlannerService;

import java.util.List;

public class AvailableRecipePage extends Page {
    MealPlannerService mealPlanner;
    private ListView<Recipe> availableRecipesListView;

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
        List<Recipe> available = mealPlanner.getAvailableRecipes();
        availableRecipesListView.getItems().addAll(available);
    }

    @Override
    public void onShow() {
        refreshAvailableRecipes();
    }
}
