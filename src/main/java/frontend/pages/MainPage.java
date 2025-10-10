package frontend.pages;

import frontend.NavigationButton;
import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import models.Route;

public class MainPage extends Page {
    Stage stage;

    public MainPage(Navigator navigator, Stage stage) {
        super(navigator);
        this.stage = stage;
    }

    @Override
    public Parent getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));

        Label titleLabel = new Label("HSD MealPlanner");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button recipesButton = new NavigationButton("Rezepte verwalten", Route.RECIPE, navigator);
        Button pantryButton = new NavigationButton("Speisekammer verwalten", Route.PANTRY, navigator);
        Button mealPlansButton = new NavigationButton("Speisepläne verwalten", Route.MEALPLAN, navigator);
        Button exitButton = new Button("Beenden");

        exitButton.setOnAction(e -> stage.close());

        // Button styling
        recipesButton.setPrefWidth(200);
        pantryButton.setPrefWidth(200);
        mealPlansButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);

        root.getChildren().addAll(titleLabel, recipesButton, pantryButton, mealPlansButton,
                exitButton);

        return root;
    }
}
