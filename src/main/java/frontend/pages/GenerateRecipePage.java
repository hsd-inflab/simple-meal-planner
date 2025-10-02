package frontend.pages;

import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import models.Recipe;
import models.RecipeIngredient;
import models.Route;

import services.MealPlannerService;
import services.RecipeAPIService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateRecipePage extends Page {
    private MealPlannerService mealPlanner;
    private RecipeAPIService recipeAPIService;
    // Hint text shown in the recipe list before any search
    private static final String RECIPE_LIST_HINT = "Zutaten oder Titel eingeben und passenden Such-Button klicken.";
    private Label detailsContentLabel;
    private TextArea detailsTextArea; // For recipe details display

    public GenerateRecipePage(Navigator navigator, MealPlannerService mealPlanner, RecipeAPIService recipeAPIService) {
        super(navigator);
        this.mealPlanner = mealPlanner;
        this.recipeAPIService = recipeAPIService;
    }

    @Override
    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Neues Rezept generieren");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.setTop(titleLabel);

        // Left: Ingredients List + Title Search Box
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(20, 0, 0, 0)); // Mehr Abstand oben
        Label ingredientsLabel = new Label("Zutaten:");
        ingredientsLabel.setStyle("-fx-font-weight: bold;");

        VBox ingredientsBox = new VBox(10);
        ScrollPane ingredientsScroll = new ScrollPane(ingredientsBox);
        ingredientsScroll.setPrefHeight(300);
        ingredientsScroll.setPrefWidth(300);

        // Add first ingredient row
        collectIngredientsRow(ingredientsBox);

        // Title search controls with header label
        Label titleSearchHeader = new Label("Titel des Rezepts:");
        titleSearchHeader.setStyle("-fx-font-weight: bold;");
        TextField titleSearchField = new TextField();
        titleSearchField.setPromptText("Titel eingeben");
        Button titleSearchButton = new Button("Rezepte nach Titel suchen");

        // Ingredients-based search button should be directly under the ingredients box
        Button ingredientsSearchButton = new Button("Rezepte nach Zutaten suchen");

        // Spacer to push title search controls to bottom-left
        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        // Wrap title search controls in a box that extends to the bottom
        VBox titleSearchBox = new VBox(8);
        titleSearchBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10;");
        Region titleBoxSpacer = new Region();
        VBox.setVgrow(titleBoxSpacer, Priority.ALWAYS);
        titleSearchBox.getChildren().addAll(titleSearchField, titleBoxSpacer);
        VBox.setVgrow(titleSearchBox, Priority.ALWAYS);

        // small gap between ingredients button and title label
        Region gapBetweenButtonsAndTitle = new Region();
        gapBetweenButtonsAndTitle.setMinHeight(8);
        gapBetweenButtonsAndTitle.setPrefHeight(8);

        leftBox.getChildren().addAll(ingredientsLabel, ingredientsScroll, ingredientsSearchButton, gapBetweenButtonsAndTitle, titleSearchHeader, titleSearchBox, titleSearchButton, leftSpacer);

        root.setLeft(leftBox);

        // Center: Generated recipes
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(20, 10, 0, 10));
        Label detailsLabel = new Label("Rezepte:");
        detailsLabel.setStyle("-fx-font-weight: bold;");

        // Create buttons
        Button saveRecipeButton = new Button("Rezept speichern");
        Button backButton = new Button("Zurück zu Rezepten");

        // Initially hide "Rezept speichern" button
        saveRecipeButton.setVisible(false);

        ListView<String> recipeListView = new ListView<>();
        recipeListView.setPrefHeight(300);
        recipeListView.setPrefWidth(400);
        recipeListView.setOnMouseClicked(event -> {
            if (recipeListView.isMouseTransparent()) {
                return; // Do not react to clicks when showing hint
            }
            String selectedRecipe = recipeListView.getSelectionModel().getSelectedItem();
            if (selectedRecipe != null) {
                if (selectedRecipe.equals("Keine Rezepte gefunden.") || selectedRecipe.equals(RECIPE_LIST_HINT) || selectedRecipe.startsWith("Fehler beim Laden der Rezepte:")) {
                    return; // ignore clicks on placeholder/error rows
                }
                // Show confirmation dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Rezeptdetails anzeigen");
                alert.setHeaderText(null);
                alert.setContentText("Möchten Sie die Details für \"" + selectedRecipe + "\" anzeigen?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    showRecipeDetails(selectedRecipe, detailsContentLabel);
                    // Show "Rezept speichern" button only when a real recipe is selected
                    if (!selectedRecipe.equals("Keine Rezepte gefunden.") && !selectedRecipe.equals(RECIPE_LIST_HINT)) {

                        saveRecipeButton.setVisible(true);
                    }
                }
            }
        });

        // Initial hint and non-interactive state
        recipeListView.getItems().add(RECIPE_LIST_HINT);
        recipeListView.setMouseTransparent(true);

        // Create scrollable details area using TextArea for better long text handling
        this.detailsTextArea = new TextArea("Hier stehen die Details zum ausgewählten Rezept.");
        this.detailsTextArea.setWrapText(true);
        this.detailsTextArea.setEditable(false); // Read-only
        this.detailsTextArea.setPrefColumnCount(50);
        this.detailsTextArea.setPrefRowCount(15);
        this.detailsTextArea.setStyle("-fx-font-size: 12px; -fx-font-family: 'Monospaced';"); // Monospaced font for
        // better readability

        // Store reference for later use
        detailsContentLabel = new Label(); // Keep for compatibility, but won't be used for display
        detailsContentLabel.setVisible(false); // Hide the label

        ScrollPane detailsScrollPane = new ScrollPane(detailsTextArea);
        detailsScrollPane.setPrefHeight(300);
        detailsScrollPane.setPrefWidth(400);
        detailsScrollPane.setFitToWidth(true);
        detailsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        detailsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        centerBox.getChildren().addAll(detailsLabel, recipeListView, detailsScrollPane);
        root.setCenter(centerBox);

        // Save recipe functionality
        saveRecipeButton.setOnAction(e -> {
            String selectedRecipe = recipeListView.getSelectionModel().getSelectedItem();

            if (selectedRecipe != null && !selectedRecipe.equals("Keine Rezepte gefunden.") && !selectedRecipe.equals(RECIPE_LIST_HINT)) {

                saveGeneratedRecipe(selectedRecipe);
            }
        });

        backButton.setOnAction(e -> {
            clearAddRecipeForm(ingredientsBox, recipeListView);
            detailsContentLabel.setText("Hier stehen die Details zum ausgewählten Rezept.");
            if (detailsTextArea != null) {
                detailsTextArea.setText("Hier stehen die Details zum ausgewählten Rezept.");
            }
            saveRecipeButton.setVisible(false);
            // Clear title search input when leaving this screen
            titleSearchField.clear();
            navigator.show(Route.RECIPE);
        });

        ingredientsSearchButton.setOnAction(e -> unifiedSearch(collectTermsFromIngredients(ingredientsBox), recipeListView, RecipeAPIService.SearchMode.INGREDIENTS_ONLY));
        titleSearchButton.setOnAction(e -> unifiedSearch(List.of(titleSearchField.getText()), recipeListView, RecipeAPIService.SearchMode.TITLE_ONLY));

        // Bottom-left of details box: Buttons inside center column
        HBox centerButtonBox = new HBox(10);
        centerButtonBox.setAlignment(Pos.CENTER_LEFT);
        centerButtonBox.setPadding(new Insets(10, 0, 0, 0));
        centerButtonBox.getChildren().addAll(backButton, saveRecipeButton);
        centerBox.getChildren().add(centerButtonBox);

        return root;
    }

    private void collectIngredientsRow(VBox ingredientsBox) {
        HBox ingredientRow = new HBox(10);
        ingredientRow.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Zutat");
        nameField.setPrefWidth(120);
        nameField.setOnAction(e -> {
            collectIngredientsRow(ingredientsBox);
            ingredientRow.requestFocus();
        });

        Button addButton = new Button("Hinzufügen");
        addButton.setOnAction(e -> {
            if (nameField.getText().isEmpty()) {
                showError("Fehler", "Eingabefeld für Zutat ist leer!");
            } else {
                collectIngredientsRow(ingredientsBox);
            }
        });

        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e -> {
            if (ingredientsBox.getChildren().size() > 1) {
                ingredientsBox.getChildren().remove(ingredientRow);
            } else {
                nameField.clear();
            }
        });

        ingredientsBox.getChildren().add(ingredientRow);
        ingredientRow.getChildren().addAll(nameField, addButton, removeButton);
    }

    private void showRecipeDetails(String recipeTitle, Label detailsLabel) {
        try {
            String details = recipeAPIService.getRecipeDetails(recipeTitle);
            detailsLabel.setText(details);

            // Update the TextArea with the full recipe details
            if (detailsTextArea != null) {
                detailsTextArea.setText(details);
            }
        } catch (Exception e) {
            String errorMessage = "Fehler beim Laden der Rezeptdetails: " + e.getMessage();
            detailsLabel.setText(errorMessage);
            if (detailsTextArea != null) {
                detailsTextArea.setText(errorMessage);
            }
        }
    }

    private void saveGeneratedRecipe(String recipeTitle) {
        try {
            // Fetch full recipe data (title, description, ingredients) from API
            RecipeAPIService.RecipeData data = recipeAPIService.fetchRecipeData(recipeTitle);

            List<RecipeIngredient> ingredients = data.getIngredients();
            String description = data.getDescription();

            Recipe recipe = new Recipe(recipeTitle, description, ingredients);
            mealPlanner.getRecipeBook().add(recipe);
            // mealPlanner.saveRecipeBookNow();

            showInfo("Erfolg", "Rezept \"" + recipeTitle + "\" wurde erfolgreich gespeichert!");

        } catch (Exception e) {
            showError("Fehler", "Fehler beim Speichern des Rezepts: " + e.getMessage());
        }
    }

    private void clearAddRecipeForm(VBox ingredientsBox, ListView<String> recipeListView) {
        ingredientsBox.getChildren().clear();
        recipeListView.getItems().clear();
        recipeListView.getItems().add(RECIPE_LIST_HINT);
        recipeListView.setMouseTransparent(true);
        collectIngredientsRow(ingredientsBox);
    }

    private void unifiedSearch(List<String> terms, ListView<String> recipeListView, RecipeAPIService.SearchMode mode) {
        List<String> cleaned = new ArrayList<>();
        if (terms != null) {
            for (String t : terms) {
                String c = t == null ? "" : t.trim();
                if (!c.isEmpty()) cleaned.add(c);
            }
        }
        if (cleaned.isEmpty()) {
            showError("Fehler", "Bitte geben Sie Suchbegriffe ein.");
            return;
        }
        try {
            List<String> recipeTitles = recipeAPIService.searchRecipeTitles(cleaned, mode);
            recipeListView.getItems().clear();
            recipeListView.setMouseTransparent(false);
            if (!recipeTitles.isEmpty()) {
                recipeListView.getItems().addAll(recipeTitles);
                recipeListView.setMouseTransparent(false);
            } else {
                recipeListView.getItems().add("Keine Rezepte gefunden.");
                recipeListView.setMouseTransparent(true);
            }
        } catch (Exception e) {
            recipeListView.getItems().clear();
            recipeListView.getItems().add("Fehler beim Laden der Rezepte: " + e.getMessage());
            recipeListView.setMouseTransparent(true);
        }
    }

    private List<String> collectTermsFromIngredients(VBox ingredientsBox) {
        List<String> ingredients = new ArrayList<>();
        for (var node : ingredientsBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField nameF = (TextField) row.getChildren().get(0);
                String ingName = nameF.getText().trim();
                if (!ingName.isEmpty()) {
                    ingredients.add(ingName);
                }
            }
        }
        return ingredients;
    }

    private void generateRecipe(VBox ingredientsBox, ListView<String> recipeListView) {
        List<String> ingredients = new ArrayList<>();
        for (var node : ingredientsBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField nameF = (TextField) row.getChildren().get(0);
                String ingName = nameF.getText().trim();
                if (!ingName.isEmpty()) {
                    ingredients.add(ingName);
                }
            }
        }

        if (!ingredients.isEmpty()) {
            try {
                List<String> recipeTitles = recipeAPIService.searchRecipeTitles(ingredients);
                recipeListView.getItems().clear();
                if (!recipeTitles.isEmpty()) {
                    recipeListView.getItems().addAll(recipeTitles);
                    recipeListView.setMouseTransparent(false);
                } else {
                    recipeListView.getItems().add("Keine Rezepte gefunden.");
                    recipeListView.setMouseTransparent(true);
                }
            } catch (Exception e) {
                recipeListView.getItems().clear();
                recipeListView.getItems().add("Fehler beim Laden der Rezepte: " + e.getMessage());
                recipeListView.setMouseTransparent(true);
            }
        } else {
            recipeListView.getItems().clear();
            recipeListView.getItems().add("Keine Zutaten hinzugefügt.");
            recipeListView.setMouseTransparent(true);
        }
    }
}
