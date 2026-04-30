package hsd.inflab.smp.frontend.pages;

import hsd.inflab.smp.dto.RecipeAPIDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Route;
import hsd.inflab.smp.enums.SearchMode;
import hsd.inflab.smp.frontend.Navigator;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.service.RecipeAPIService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GenerateRecipePage extends Page {
    private final MealPlannerService mealPlanner;
    private final RecipeAPIService recipeAPIService;
    // Hint text shown in the recipe list before any search
    private static final String RECIPE_LIST_HINT = "Zutaten oder Titel eingeben und passenden Such-Button klicken.";
    private static final String NO_RECIPES_FOUND = "Keine Rezepte gefunden.";
    private static final String DEFAULT_DETAILS_TEXT = "Hier stehen die Details zum ausgewählten Rezept.";
    private static final String ERROR_PREFIX = "Fehler beim Laden der Rezepte: ";
    private static final int MIN_INGREDIENT_ROWS = 1;
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

        leftBox.getChildren()
                .addAll(
                        ingredientsLabel,
                        ingredientsScroll,
                        ingredientsSearchButton,
                        gapBetweenButtonsAndTitle,
                        titleSearchHeader,
                        titleSearchBox,
                        titleSearchButton,
                        leftSpacer);

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
            if (selectedRecipe != null && !isPlaceholderOrError(selectedRecipe)) {
                // Show confirmation dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Rezeptdetails anzeigen");
                alert.setHeaderText(null);
                alert.setContentText("Möchten Sie die Details für \"" + selectedRecipe + "\" anzeigen?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    showRecipeDetails(selectedRecipe, detailsContentLabel);
                    // Show "Rezept speichern" button only when a real recipe is selected
                    saveRecipeButton.setVisible(true);
                }
            }
        });

        // Initial hint and non-interactive state
        recipeListView.getItems().add(RECIPE_LIST_HINT);
        recipeListView.setMouseTransparent(true);

        // Create scrollable details area using TextArea for better long text handling
        this.detailsTextArea = new TextArea(DEFAULT_DETAILS_TEXT);
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

            if (selectedRecipe != null && !isPlaceholderOrError(selectedRecipe)) {
                saveGeneratedRecipe(selectedRecipe);
            }
        });

        backButton.setOnAction(e -> {
            clearAddRecipeForm(ingredientsBox, recipeListView);
            detailsContentLabel.setText(DEFAULT_DETAILS_TEXT);
            if (detailsTextArea != null) {
                detailsTextArea.setText(DEFAULT_DETAILS_TEXT);
            }
            saveRecipeButton.setVisible(false);
            // Clear title search input when leaving this screen
            titleSearchField.clear();
            navigator.show(Route.RECIPE);
        });

        ingredientsSearchButton.setOnAction(e -> unifiedSearch(
                collectTermsFromIngredients(ingredientsBox), recipeListView, SearchMode.INGREDIENTS_ONLY));
        titleSearchButton.setOnAction(
                e -> unifiedSearch(List.of(titleSearchField.getText()), recipeListView, SearchMode.TITLE_ONLY));

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
            if (ingredientsBox.getChildren().size() > MIN_INGREDIENT_ROWS) {
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
        } catch (Exception e) { // NOPMD - RecipeAPIService declares throws Exception
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
            RecipeAPIDto data = recipeAPIService.fetchRecipeData(recipeTitle);

            List<RecipeIngredient> ingredients = data.ingredients();
            String description = data.description();

            Recipe recipe = new Recipe(recipeTitle, description, ingredients);
            RecipeDto dto = mealPlanner.recipeService.convertToDtoTempWrapper(
                    recipe); // this is a hack and only for compatibility
            mealPlanner.recipeService.addRecipe(dto);
            // mealPlanner.saveRecipeBookNow();

            showInfo("Erfolg", "Rezept \"" + recipeTitle + "\" wurde erfolgreich gespeichert!");

        } catch (Exception e) { // NOPMD - RecipeAPIService declares throws Exception
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

    private void unifiedSearch(List<String> terms, ListView<String> recipeListView, SearchMode mode) {
        List<String> cleaned = new ArrayList<>();
        if (terms != null) {
            for (String t : terms) {
                String c = t == null ? "" : t.trim();
                if (!c.isEmpty()) {
                    cleaned.add(c);
                }
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
                recipeListView.getItems().add(NO_RECIPES_FOUND);
                recipeListView.setMouseTransparent(true);
            }
        } catch (Exception e) { // NOPMD - RecipeAPIService declares throws Exception
            recipeListView.getItems().clear();
            recipeListView.getItems().add(ERROR_PREFIX + e.getMessage());
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

    private boolean isPlaceholderOrError(String recipeItem) {
        return NO_RECIPES_FOUND.equals(recipeItem)
                || RECIPE_LIST_HINT.equals(recipeItem)
                || recipeItem.startsWith(ERROR_PREFIX);
    }
}
