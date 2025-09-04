package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import models.DailyMeal;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;
import services.MealPlannerService;
import services.RecipeAPIService;

public class MealPlannerFX extends Application {
    private MealPlannerService mealPlanner;
    private Stage primaryStage;
    private RecipeAPIService recipeAPIService;
    
    // Scenes
    private Scene mainMenuScene;
    private Scene recipesScene;
    private Scene addRecipeScene;
    private Scene generateRecipeScene;
    private Scene pantryScene;
    private Scene addGroceryScene;
    private Scene availableRecipesScene;
    private Scene mealPlansScene;
    private Scene addMealPlanScene;
    
    // Components für Datenaktualisierung
    private ListView<Recipe> recipeListView;
    private ListView<PantryItem> pantryListView;
    private ListView<Recipe> availableRecipesListView;
    private ListView<String> mealPlansListView;
    private Label recipeDetailsLabel;
    private Label pantryDetailsLabel;
    private Label detailsContentLabel;
    private TextArea detailsTextArea; // For recipe details display
    // Hint text shown in the recipe list before any search
    private static final String RECIPE_LIST_HINT = "Zutaten oder Titel eingeben und passenden Such-Button klicken.";

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
        this.primaryStage = primaryStage;
        //this.mealPlanner = new MealPlannerService(); // oder per Constructor injection
        
        primaryStage.setTitle("HSD MealPlanner");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);
        
        createAllScenes();
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();
    } 
    
    private void createAllScenes() {
        mainMenuScene = createMainMenuScene();
        recipesScene = createRecipesScene();
        addRecipeScene = createAddRecipeScene();
        generateRecipeScene = createGenerateRecipeScene();
        pantryScene = createPantryScene();
        addGroceryScene = createAddGroceryScene();
        availableRecipesScene = createAvailableRecipesScene();
        mealPlansScene = createMealPlansScene();
        addMealPlanScene = createAddMealPlanScene();
    }

    // ============ MAIN MENU SCENE ============
    private Scene createMainMenuScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        
        Label titleLabel = new Label("HSD MealPlanner");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button recipesButton = new Button("Rezepte verwalten");
        Button pantryButton = new Button("Speisekammer verwalten");
        Button availableButton = new Button("Mögliche Rezepte");
        Button mealPlansButton = new Button("Speisepläne verwalten");
        Button exitButton = new Button("Beenden");
        
        // Button styling
        recipesButton.setPrefWidth(200);
        pantryButton.setPrefWidth(200);
        availableButton.setPrefWidth(200);
        mealPlansButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        
        // Event handlers
        recipesButton.setOnAction(e -> {
            refreshRecipes();
            switchToScene(recipesScene);
        });
        pantryButton.setOnAction(e -> {
            refreshPantry();
            switchToScene(pantryScene);
        });
        availableButton.setOnAction(e -> {
            refreshAvailableRecipes();
            switchToScene(availableRecipesScene);
        });
        mealPlansButton.setOnAction(e -> {
            //refreshMealPlans();           //TODO: Implement refreshMealPlans method
            switchToScene(mealPlansScene);
        });
        exitButton.setOnAction(e -> {
            primaryStage.close();
        });
        
        root.getChildren().addAll(titleLabel, recipesButton, pantryButton, availableButton, mealPlansButton, exitButton);
        return new Scene(root, 1000, 700);
    }

    // ============ RECIPES SCENE ============
    private Scene createRecipesScene() {
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

        recipeDetailsLabel = new Label("Wähle ein Rezept aus der Liste");
        recipeDetailsLabel.setWrapText(true);
        recipeDetailsLabel.setMaxWidth(400);

        HBox personsBox = new HBox(10, personsLabel, personsSpinner);
        personsBox.setAlignment(Pos.CENTER_LEFT);

        centerBox.getChildren().addAll(detailsLabel, personsBox, recipeDetailsLabel);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        Button addButton = new Button("Neues Rezept hinzufügen");
        Button generateButton = new Button("Neues Rezept generieren");
        Button backButton = new Button("Zurück zum Hauptmenü");

        addButton.setOnAction(e -> switchToScene(addRecipeScene));
        generateButton.setOnAction(e -> switchToScene(generateRecipeScene));
        backButton.setOnAction(e -> switchToScene(mainMenuScene));

        buttonBox.getChildren().addAll(addButton, generateButton, backButton);
        root.setBottom(buttonBox);

        return new Scene(root, 1000, 700);
    }

    private void showRecipeDetails(Recipe recipe, int persons) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(recipe.getName()).append("\n\n");
        details.append("Beschreibung: ").append(recipe.getDescription()).append("\n\n");
        details.append("Zutaten (für ").append(persons).append(" Person").append(persons > 1 ? "en" : "").append("):\n");

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            double totalAmount = ingredient.getAmount() * persons;
            details.append("- ").append(ingredient.getName())
                   .append(" (").append(totalAmount)
                   .append(" ").append(ingredient.getUnit()).append(")\n");
        }

        recipeDetailsLabel.setText(details.toString());
    }

    // ============ ADD RECIPE SCENE ============
    private Scene createAddRecipeScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Neues Rezept hinzufügen");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Recipe name and description
        TextField nameField = new TextField();
        nameField.setPromptText("Rezeptname");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Beschreibung");
        descriptionArea.setPrefRowCount(3);
        
        // Ingredients section
        Label ingredientsLabel = new Label("Zutaten:");
        ingredientsLabel.setStyle("-fx-font-weight: bold;");
        
        VBox ingredientsBox = new VBox(10);
        ScrollPane ingredientsScroll = new ScrollPane(ingredientsBox);
        ingredientsScroll.setPrefHeight(300);
        
        Button addIngredientButton = new Button("Zutat hinzufügen");
        addIngredientButton.setOnAction(e -> addIngredientRow(ingredientsBox));
        
        // Add first ingredient row
        addIngredientRow(ingredientsBox);
        
        // Bottom buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button("Speichern");
        Button backButton = new Button("Zurück zu Rezepten");
        
        saveButton.setOnAction(e -> {
            if (saveRecipe(nameField, descriptionArea, ingredientsBox)) {
                clearAddRecipeForm(nameField, descriptionArea, ingredientsBox);
                refreshRecipes();
                switchToScene(recipesScene);
            }
        });
        backButton.setOnAction(e -> {
            clearAddRecipeForm(nameField, descriptionArea, ingredientsBox);
            switchToScene(recipesScene);
        });
        
        buttonBox.getChildren().addAll(saveButton, backButton);
        
        root.getChildren().addAll(titleLabel, new Label("Name:"), nameField, 
                                  new Label("Beschreibung:"), descriptionArea,
                                  ingredientsLabel, ingredientsScroll, addIngredientButton, buttonBox);
        
        return new Scene(new ScrollPane(root), 1000, 700);
    }
    
    private void addIngredientRow(VBox ingredientsBox) {
        HBox ingredientRow = new HBox(10);
        ingredientRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Zutat");
        nameField.setPrefWidth(120);
        
        TextField unitField = new TextField();
        unitField.setPromptText("Einheit");
        unitField.setPrefWidth(80);
        
        TextField amountField = new TextField();
        amountField.setPromptText("Menge");
        amountField.setPrefWidth(80);
        
        TextField categoryField = new TextField();
        categoryField.setPromptText("Kategorie");
        categoryField.setPrefWidth(100);
        
        TextField typeField = new TextField();
        typeField.setPromptText("Typ");
        typeField.setPrefWidth(100);
        
        TextField prepField = new TextField();
        prepField.setPromptText("Vorbereitung");
        prepField.setPrefWidth(120);
        
        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e -> ingredientsBox.getChildren().remove(ingredientRow));
        
        ingredientRow.getChildren().addAll(nameField, unitField, amountField, 
                                          categoryField, typeField, prepField, removeButton);
        ingredientsBox.getChildren().add(ingredientRow);
    }
    
    private boolean saveRecipe(TextField nameField, TextArea descriptionArea, VBox ingredientsBox) {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        if (name.isEmpty()) {
            showError("Fehler", "Bitte geben Sie einen Rezeptnamen ein.");    
            return false;
        }
        
        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (var node : ingredientsBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField nameF = (TextField) row.getChildren().get(0);
                TextField unitF = (TextField) row.getChildren().get(1);
                TextField amountF = (TextField) row.getChildren().get(2);
                TextField categoryF = (TextField) row.getChildren().get(3);
                TextField typeF = (TextField) row.getChildren().get(4);
                TextField prepF = (TextField) row.getChildren().get(5);
                
                String ingName = nameF.getText().trim();
                if (!ingName.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountF.getText().trim());
                        RecipeIngredient ingredient = new RecipeIngredient(
                            ingName, unitF.getText().trim(), amount,
                            categoryF.getText().trim(), typeF.getText().trim(),
                            prepF.getText().trim()
                        );
                        ingredients.add(ingredient);
                    } catch (NumberFormatException e) {
                        showError("Fehler", "Ungültige Mengenangabe bei Zutat: " + ingName);
                        return false;
                    }
                }
            }
        }
        
        if (ingredients.isEmpty()) {
            showError("Fehler", "Bitte fügen Sie mindestens eine Zutat hinzu.");
            return false;
        }
        
        Recipe recipe = new Recipe(name, description, ingredients);
        mealPlanner.getRecipeBook().add(recipe);
        return true;
    }
    
    private void clearAddRecipeForm(TextField nameField, TextArea descriptionArea, VBox ingredientsBox) {
        nameField.clear();
        descriptionArea.clear();
        ingredientsBox.getChildren().clear();
        addIngredientRow(ingredientsBox);
    }

    // ============ GENERATE RECIPE SCENE ============
    private Scene createGenerateRecipeScene() {
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
        this.detailsTextArea.setStyle("-fx-font-size: 12px; -fx-font-family: 'Monospaced';"); // Monospaced font for better readability
        
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
            switchToScene(recipesScene);
        });
        
        ingredientsSearchButton.setOnAction(e -> unifiedSearch(collectTermsFromIngredients(ingredientsBox), recipeListView, RecipeAPIService.SearchMode.INGREDIENTS_ONLY));
        titleSearchButton.setOnAction(e -> unifiedSearch(List.of(titleSearchField.getText()), recipeListView, RecipeAPIService.SearchMode.TITLE_ONLY));
        
        // Bottom-left of details box: Buttons inside center column
        HBox centerButtonBox = new HBox(10);
        centerButtonBox.setAlignment(Pos.CENTER_LEFT);
        centerButtonBox.setPadding(new Insets(10, 0, 0, 0));
        centerButtonBox.getChildren().addAll(backButton, saveRecipeButton);
        centerBox.getChildren().add(centerButtonBox);
        
        return new Scene(root, 1000, 700);
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
            }
            else {
                collectIngredientsRow(ingredientsBox);
            }
        });

        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e -> {
            if (ingredientsBox.getChildren().size() > 1) {
                    ingredientsBox.getChildren().remove(ingredientRow);
            }
            else {
                nameField.clear();
            }
        });

        ingredientsBox.getChildren().add(ingredientRow);
        ingredientRow.getChildren().addAll(nameField, addButton, removeButton);
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
                } else {
                    recipeListView.getItems().add("Keine Rezepte gefunden.");
                }
            } catch (Exception e) {
                recipeListView.getItems().clear();
                recipeListView.getItems().add("Fehler beim Laden der Rezepte: " + e.getMessage());
            }
        } else {
            recipeListView.getItems().clear();
            recipeListView.getItems().add("Keine Zutaten hinzugefügt.");
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
            } else {
                recipeListView.getItems().add("Keine Rezepte gefunden.");
            }
        } catch (Exception e) {
            recipeListView.getItems().clear();
            recipeListView.setMouseTransparent(false);
            recipeListView.getItems().add("Fehler beim Laden der Rezepte: " + e.getMessage());
        }
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
    
    private void clearAddRecipeForm(VBox ingredientsBox, ListView<String> recipeListView) {
        ingredientsBox.getChildren().clear();
        recipeListView.getItems().clear();
        recipeListView.getItems().add(RECIPE_LIST_HINT);
        recipeListView.setMouseTransparent(true);
        collectIngredientsRow(ingredientsBox);
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

    // ============ PANTRY SCENE ============
    private Scene createPantryScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Left: Pantry List
        VBox leftBox = new VBox(10);
        Label listLabel = new Label("Speisekammer:");
        listLabel.setStyle("-fx-font-weight: bold;");
        
        pantryListView = new ListView<>();
        pantryListView.setPrefWidth(300);
        pantryListView.setOnMouseClicked(event -> {
            PantryItem selected = pantryListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showPantryDetails(selected);
            }
        });
        
        leftBox.getChildren().addAll(listLabel, pantryListView);
        root.setLeft(leftBox);
        
        // Center: Item Details
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(0, 10, 0, 10));
        Label detailsLabel = new Label("Item Details:");
        detailsLabel.setStyle("-fx-font-weight: bold;");
        
        pantryDetailsLabel = new Label("Wähle ein Item aus der Liste");
        pantryDetailsLabel.setWrapText(true);
        pantryDetailsLabel.setMaxWidth(400);
        
        centerBox.getChildren().addAll(detailsLabel, pantryDetailsLabel);
        root.setCenter(centerBox);
        
        // Bottom: Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button addButton = new Button("Lebensmittel hinzufügen");
        Button backButton = new Button("Zurück zum Hauptmenü");
        
        addButton.setOnAction(e -> switchToScene(addGroceryScene));
        backButton.setOnAction(e -> switchToScene(mainMenuScene));
        
        buttonBox.getChildren().addAll(addButton, backButton);
        root.setBottom(buttonBox);
        
        return new Scene(root, 1000, 700);
    }
    
    private void showPantryDetails(PantryItem item) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(item.getName()).append("\n");
        details.append("Menge: ").append(item.getAmount()).append(" ").append(item.getUnit()).append("\n");
        details.append("Kategorie: ").append(item.getCategory()).append("\n");
        details.append("Marke: ").append(item.getBrand()).append("\n");
        details.append("Preis: ").append(item.getPrice()).append("€\n");
        details.append("Gekauft am: ").append(item.getPurchaseDate()).append("\n");
        details.append("Verfällt am: ").append(item.getExpirationDate());
        
        pantryDetailsLabel.setText(details.toString());
    }

    // ============ ADD GROCERY SCENE ============
    private Scene createAddGroceryScene() {
        GridPane root = new GridPane();
        root.setHgap(10);
        root.setVgap(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Lebensmittel hinzufügen");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.add(titleLabel, 0, 0, 2, 1);
        
        // Input fields
        TextField nameField = new TextField();
        TextField unitField = new TextField();
        TextField amountField = new TextField();
        TextField categoryField = new TextField();
        TextField daysField = new TextField();
        TextField brandField = new TextField();
        TextField priceField = new TextField();
        
        // Labels and fields
        root.add(new Label("Name:"), 0, 1);
        root.add(nameField, 1, 1);
        root.add(new Label("Einheit:"), 0, 2);
        root.add(unitField, 1, 2);
        root.add(new Label("Menge:"), 0, 3);
        root.add(amountField, 1, 3);
        root.add(new Label("Kategorie:"), 0, 4);
        root.add(categoryField, 1, 4);
        root.add(new Label("Tage bis Ablauf:"), 0, 5);
        root.add(daysField, 1, 5);
        root.add(new Label("Marke:"), 0, 6);
        root.add(brandField, 1, 6);
        root.add(new Label("Preis:"), 0, 7);
        root.add(priceField, 1, 7);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button("Speichern");
        Button backButton = new Button("Zurück zur Speisekammer");
        
        saveButton.setOnAction(e -> {
            if (saveGrocery(nameField, unitField, amountField, categoryField, 
                           daysField, brandField, priceField)) {
                clearGroceryForm(nameField, unitField, amountField, categoryField,
                               daysField, brandField, priceField);
                refreshPantry();
                switchToScene(pantryScene);
            }
        });
        backButton.setOnAction(e -> {
            clearGroceryForm(nameField, unitField, amountField, categoryField,
                           daysField, brandField, priceField);
            switchToScene(pantryScene);
        });
        
        buttonBox.getChildren().addAll(saveButton, backButton);
        root.add(buttonBox, 0, 8, 2, 1);
        
        return new Scene(root, 1000, 700);
    }
    
    private boolean saveGrocery(TextField nameField, TextField unitField, TextField amountField,
                               TextField categoryField, TextField daysField, TextField brandField,
                               TextField priceField) {
        try {
            String name = nameField.getText().trim();
            String unit = unitField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());
            String category = categoryField.getText().trim();
            int days = Integer.parseInt(daysField.getText().trim());
            String brand = brandField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            
            if (name.isEmpty()) {
                showError("Fehler", "Bitte geben Sie einen Namen ein.");
                return false;
            }
            
            LocalDate purchaseDate = LocalDate.now();
            LocalDate expirationDate = purchaseDate.plusDays(days);
            
            PantryItem item = new PantryItem(name, unit, amount, category,
                                           expirationDate, purchaseDate,
                                           brand, price);
            mealPlanner.getPantry().add(item);
            return true;
            
        } catch (NumberFormatException e) {
            showError("Fehler", "Bitte überprüfen Sie die Zahlenangaben.");
            return false;
        }
    }
    
    private void clearGroceryForm(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    // ============ AVAILABLE RECIPES SCENE ============
    private Scene createAvailableRecipesScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Rezepte die du kochen kannst:");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        availableRecipesListView = new ListView<>();
        availableRecipesListView.setPrefHeight(400);
        
        Button backButton = new Button("Zurück zum Hauptmenü");
        backButton.setOnAction(e -> switchToScene(mainMenuScene));
        
        root.getChildren().addAll(titleLabel, availableRecipesListView, backButton);
        return new Scene(root, 1000, 700);
    }

    // ============ MEAL PLANS SCENE ============
    private Scene createMealPlansScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Speisepläne ab " + 
                                   LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        mealPlansListView = new ListView<>();
        mealPlansListView.setPrefHeight(400);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addButton = new Button("Neuen Speiseplan hinzufügen");
        Button backButton = new Button("Zurück zum Hauptmenü");
        
        addButton.setOnAction(e -> switchToScene(addMealPlanScene));
        backButton.setOnAction(e -> switchToScene(mainMenuScene));
        
        buttonBox.getChildren().addAll(addButton, backButton);
        root.getChildren().addAll(titleLabel, mealPlansListView, buttonBox);

        refreshMealPlans();
        
        return new Scene(root, 1000, 700);
    }

    // ============ ADD MEAL PLAN SCENE ============
    private Scene createAddMealPlanScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Neuen Speiseplan hinzufügen");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Date input section
        Label dateLabel = new Label("Datum auswählen:");
        dateLabel.setStyle("-fx-font-weight: bold;");
        
        ToggleGroup dateGroup = new ToggleGroup();
        RadioButton dateRadio = new RadioButton("Datum (YYYY-MM-DD)");
        RadioButton daysRadio = new RadioButton("Tage ab heute");
        RadioButton weekdayRadio = new RadioButton("Wochentag");
        
        dateRadio.setToggleGroup(dateGroup);
        daysRadio.setToggleGroup(dateGroup);
        weekdayRadio.setToggleGroup(dateGroup);
        dateRadio.setSelected(true);
        
        TextField dateField = new TextField();
        TextField daysField = new TextField();
        ComboBox<String> weekdayBox = new ComboBox<>();
        weekdayBox.getItems().addAll("Montag", "Dienstag", "Mittwoch", "Donnerstag",
                                     "Freitag", "Samstag", "Sonntag");
        
        // Meal selection
        Label mealsLabel = new Label("Mahlzeiten:");
        mealsLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<Recipe> breakfastBox = new ComboBox<>();
        ComboBox<Recipe> lunchBox = new ComboBox<>();
        ComboBox<Recipe> dinnerBox = new ComboBox<>();

        // Populate ComboBoxes with available recipes
        breakfastBox.getItems().addAll(mealPlanner.getRecipeBook());
        lunchBox.getItems().addAll(mealPlanner.getRecipeBook());
        dinnerBox.getItems().addAll(mealPlanner.getRecipeBook());

        Label personsLabel = new Label("Anzahl Personen pro Mahlzeit:");
        personsLabel.setStyle("-fx-font-weight: bold;");

        Spinner<Integer> breakfastPersonsSpinner = new Spinner<>(1, 20, 1);
        Spinner<Integer> lunchPersonsSpinner = new Spinner<>(1, 20, 1);
        Spinner<Integer> dinnerPersonsSpinner = new Spinner<>(1, 20, 1);

        HBox breakfastRow = new HBox(10, new Label("Frühstück:"), breakfastBox, new Label("Personen:"), breakfastPersonsSpinner);
        HBox lunchRow = new HBox(10, new Label("Mittagessen:"), lunchBox, new Label("Personen:"), lunchPersonsSpinner);
        HBox dinnerRow = new HBox(10, new Label("Abendessen:"), dinnerBox, new Label("Personen:"), dinnerPersonsSpinner);

        breakfastRow.setAlignment(Pos.CENTER_LEFT);
        lunchRow.setAlignment(Pos.CENTER_LEFT);
        dinnerRow.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Speichern");
        Button backButton = new Button("Zurück zu Speiseplänen");

        saveButton.setOnAction(e -> {
            if (saveMealPlan(dateGroup, dateField, daysField, weekdayBox,
                    breakfastBox, lunchBox, dinnerBox,
                    breakfastPersonsSpinner.getValue(), lunchPersonsSpinner.getValue(), dinnerPersonsSpinner.getValue())) {
                clearMealPlanForm(dateField, daysField, weekdayBox,
                        breakfastBox, lunchBox, dinnerBox);
                refreshMealPlans();
                switchToScene(mealPlansScene);
            }
        });
        backButton.setOnAction(e -> {
            clearMealPlanForm(dateField, daysField, weekdayBox,
                    breakfastBox, lunchBox, dinnerBox);
            switchToScene(mealPlansScene);
        });

        buttonBox.getChildren().addAll(saveButton, backButton);

        root.getChildren().addAll(titleLabel, dateLabel, dateRadio, dateField,
                daysRadio, daysField, weekdayRadio, weekdayBox,
                mealsLabel,
                breakfastRow,
                lunchRow,
                dinnerRow,
                buttonBox);

        return new Scene(new ScrollPane(root), 1000, 700);
    }

    private boolean saveMealPlan(ToggleGroup dateGroup, TextField dateField, TextField daysField,
                                 ComboBox<String> weekdayBox, ComboBox<Recipe> breakfastBox,
                                 ComboBox<Recipe> lunchBox, ComboBox<Recipe> dinnerBox,
                                 int breakfastPersons, int lunchPersons, int dinnerPersons) {
        try {
            LocalDate date = null;
            RadioButton selected = (RadioButton) dateGroup.getSelectedToggle();

            if (selected.getText().contains("Datum")) {
                date = LocalDate.parse(dateField.getText().trim());
            } else if (selected.getText().contains("Tage")) {
                int days = Integer.parseInt(daysField.getText().trim());
                date = LocalDate.now().plusDays(days);
            } else if (selected.getText().contains("Wochentag")) {
                String weekday = weekdayBox.getValue();
                if (weekday == null) {
                    showError("Fehler", "Bitte wählen Sie einen Wochentag.");
                    return false;
                }
                date = getNextWeekday(weekday);
            }

            if (date == null || date.isBefore(LocalDate.now())) {
                showError("Fehler", "Ungültiges Datum oder Datum liegt in der Vergangenheit.");
                return false;
            }

            DailyMeal dailyMeal = new DailyMeal();
            dailyMeal.setBreakfast(breakfastBox.getValue());
            dailyMeal.setLunch(lunchBox.getValue());
            dailyMeal.setDinner(dinnerBox.getValue());
            dailyMeal.setNumberOfPersonsBreakfast(breakfastPersons);
            dailyMeal.setNumberOfPersonsLunch(lunchPersons);
            dailyMeal.setNumberOfPersonsDinner(dinnerPersons);

            mealPlanner.getDailyMealPlans().put(date, dailyMeal);
            return true;

        } catch (Exception e) {
            showError("Fehler", "Ungültige Eingabe: " + e.getMessage());
            return false;
        }
    }
        

    
    private LocalDate getNextWeekday(String weekday) {
        LocalDate date = LocalDate.now();
        int targetDay = switch (weekday) {
            case "Montag" -> 1;
            case "Dienstag" -> 2;
            case "Mittwoch" -> 3;
            case "Donnerstag" -> 4;
            case "Freitag" -> 5;
            case "Samstag" -> 6;
            case "Sonntag" -> 7;
            default -> 0;
        };
        
        while (date.getDayOfWeek().getValue() != targetDay) {
            date = date.plusDays(1);
        }
        return date;
    }
    
    private void clearMealPlanForm(TextField dateField, TextField daysField,
                                  ComboBox<String> weekdayBox, ComboBox<Recipe>... comboBoxes) {
        dateField.clear();
        daysField.clear();
        weekdayBox.setValue(null);
        for (ComboBox<Recipe> box : comboBoxes) {
            box.setValue(null);
        }
    }

    // ============ UTILITY METHODS ============
    private void switchToScene(Scene scene) {
        primaryStage.setScene(scene);
    }
    
    private void refreshRecipes() {
        recipeListView.getItems().clear();
        recipeListView.getItems().addAll(mealPlanner.getRecipeBook());
        recipeDetailsLabel.setText("Wähle ein Rezept aus der Liste");
    }
    
    private void refreshPantry() {
        pantryListView.getItems().clear();
        pantryListView.getItems().addAll(mealPlanner.getPantry());
        pantryDetailsLabel.setText("Wähle ein Item aus der Liste");
    }
    
    private void refreshAvailableRecipes() {
        availableRecipesListView.getItems().clear();
        List<Recipe> available = getAvailableRecipes(mealPlanner.getRecipeBook(), mealPlanner.getPantry());
        availableRecipesListView.getItems().addAll(available);
    }

    private void refreshMealPlans() {
        //this currently does not fully fullfil the US 9 because the ingredient amounts are not adjusted to the number of persons
        //but since the whole display of meal plans is to be reworked in a future user story, this stays as it is for now.

        mealPlansListView.getItems().clear();
        Map<LocalDate, DailyMeal> plans = mealPlanner.getDailyMealPlans();

        List<String> items = plans.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
            .collect(Collectors.toList());

        mealPlansListView.getItems().setAll(items);
    }

    private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
    }

    public List<Recipe> getAvailableRecipes(List<Recipe> recipeBook, List<PantryItem> pantry) {
        List<Recipe> availableRecipes = new ArrayList<>();

        for (Recipe recipe : recipeBook) {
            boolean canMake = true;

            // Durchlaufe alle Zutaten des Rezepts
            for (RecipeIngredient recipeIng : recipe.getIngredients()) {
                // Suche nach der Zutat in der Pantry
                Optional<PantryItem> matchingItem = pantry.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(recipeIng.getName()))  // Vergleiche die Namen der Zutaten
                        .findFirst();  // Finde das erste PantryItem, das der Zutat entspricht

                // Prüfe, ob die Zutat in der Pantry vorhanden ist und ob die Menge ausreicht
                if (matchingItem.isEmpty() || matchingItem.get().getAmount() < recipeIng.getAmount()) {
                    canMake = false;  // Rezept kann nicht gemacht werden, da Zutat fehlt oder Menge nicht ausreicht
                    break;  // Schleife abbrechen, da es nicht mehr möglich ist, das Rezept zu machen
                }
            }

            // Wenn das Rezept mit den Zutaten zubereitet werden kann, füge es zur Liste der verfügbaren Rezepte hinzu
            if (canMake) {
                availableRecipes.add(recipe);
            }
        }

        return availableRecipes;
    }
}

