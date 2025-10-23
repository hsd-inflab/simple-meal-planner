package frontend.pages;

import frontend.AppState;
import frontend.NavigationButton;
import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import models.Recipe;
import models.RecipeIngredient;
import models.Route;
import models.Unit;

import services.MealPlannerService;

public class RecipePage extends Page {
    private ListView<Recipe> recipeListView;
    private TextArea recipeDetailsTextArea;
    private MealPlannerService mealPlanner;
    private AppState appState;

    public RecipePage(Navigator navigator, MealPlannerService mealPlanner, AppState appState) {
        super(navigator);
        this.mealPlanner = mealPlanner;
        this.appState = appState;
    }

    @Override
    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Spinner for number of persons (declare before recipeListView for scope)
        Label personsLabel = new Label("Anzahl Personen:");
        personsLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> personsSpinner = new Spinner<>(1, 20, 1);
        personsSpinner.setEditable(true);

        // Left: Recipe List
        VBox leftBox = new VBox(10);
        Label listLabel = new Label("Rezepte:");
        listLabel.setStyle("-fx-font-weight: bold;");

        recipeListView = new ListView<>();
        recipeListView.setPrefWidth(300);
        recipeListView.setOnMouseClicked(event -> {
            Recipe selected = recipeListView.getSelectionModel().getSelectedItem();
            appState.setSelectedRecipe(selected);
            if (selected != null) {
                showRecipeDetails(selected, personsSpinner.getValue());
            }
        });

        leftBox.getChildren().addAll(listLabel, recipeListView);
        root.setLeft(leftBox);

        // Center: Recipe Details and Persons Spinner
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(0, 10, 0, 10));
        Label detailsLabel = new Label("Rezept Details:");
        detailsLabel.setStyle("-fx-font-weight: bold;");

        // Update details when personsSpinner changes
        personsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Recipe selected = recipeListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRecipeDetails(selected, newVal);
            }
        });

        recipeDetailsTextArea = new TextArea("Wähle ein Rezept aus der Liste");
        recipeDetailsTextArea.setWrapText(true);

        recipeDetailsTextArea.setMaxWidth(500);
        recipeDetailsTextArea.setMinHeight(100);
        recipeDetailsTextArea.setPrefHeight(350);
        recipeDetailsTextArea.setMaxHeight(1200);
        recipeDetailsTextArea.setEditable(false);

        HBox personsBox = new HBox(10, personsLabel, personsSpinner);
        personsBox.setAlignment(Pos.CENTER_LEFT);

        centerBox.getChildren().addAll(detailsLabel, personsBox, recipeDetailsTextArea);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        Button addButton = new NavigationButton("Neues Rezept hinzufügen", Route.ADD_RECIPE, navigator);
        Button editButton = new Button("Rezept bearbeiten");
        Button generateButton = new Button("Neues Rezept generieren");
        Button backButton = new NavigationButton("Zurück zum Hauptmenü", Route.MAIN, navigator);

        editButton.setOnAction(e -> {
            Recipe selected = recipeListView.getSelectionModel().getSelectedItem();
            appState.setSelectedRecipe(selected);
            if (appState.getSelectedRecipe() != null) {
                navigator.show(Route.EDIT_RECIPE);
            } else {
                showError("Fehler", "Bitte wählen Sie ein Rezept zum Bearbeiten aus.");
            }
        });
        generateButton.setOnAction(e -> passwordCheckDialog());

        buttonBox.getChildren().addAll(addButton, editButton, generateButton, backButton);
        root.setBottom(buttonBox);

        return root;
    }

    private void showRecipeDetails(Recipe recipe, int persons) {
        StringBuilder details = new StringBuilder();
        details.append("Name: \n").append(recipe.getName()).append("\n\n");
        details.append("Beschreibung: \n").append(recipe.getDescription()).append("\n\n");
        details.append("Zutaten (für ").append(persons).append(" Person").append(persons > 1 ? "en" : "")
                .append("):\n");

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            double totalAmount = ingredient.getAmount() * persons;
            details.append("- ").append(ingredient.getName());
            if (ingredient.getUnit() != Unit.NONE && ingredient.getUnit() != null) {
                details.append(" (").append(totalAmount)
                        .append(" ").append(localizedUnitMap.get(ingredient.getUnit())).append(")");
            }
            details.append("\n");
        }

        recipeDetailsTextArea.setText(details.toString());
    }

    private void passwordCheckDialog() {
        final boolean correctPasswordEntered;
        final String password;
        Dialog<String> dialog = new Dialog<>();

        dialog.setTitle("Passworteingabe erforderlich");
        dialog.setHeaderText("Passwort eingeben:");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Passwort");
        dialog.getDialogPane().setContent(pwd);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return pwd.getText();
            }
            return null;
        });

        password = dialog.showAndWait().orElse("");
        correctPasswordEntered = mealPlanner.verifyAPIPassword(password);
        if (correctPasswordEntered)
            navigator.show(Route.GENERATE_RECIPE);
    }

    private void refreshRecipes() {
        recipeListView.getItems().clear();
        recipeListView.getItems().addAll(mealPlanner.getRecipeBook());
        recipeDetailsTextArea.setText("Wähle ein Rezept aus der Liste");
    }

    @Override
    public void onShow() {
        refreshRecipes();
    }
}
