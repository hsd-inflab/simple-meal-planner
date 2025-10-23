package frontend.pages;

import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import models.*;

import services.MealPlannerService;

import java.util.ArrayList;
import java.util.List;

public class AddRecipePage extends Page {
    private MealPlannerService mealPlanner;

    public AddRecipePage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
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
                navigator.show(Route.RECIPE);
            }
        });
        backButton.setOnAction(e -> {
            clearAddRecipeForm(nameField, descriptionArea, ingredientsBox);
            navigator.show(Route.RECIPE);
        });

        buttonBox.getChildren().addAll(saveButton, backButton);

        root.getChildren().addAll(titleLabel, new Label("Name:"), nameField,
                new Label("Beschreibung:"), descriptionArea,
                ingredientsLabel, ingredientsScroll, addIngredientButton, buttonBox);

        return root;
    }

    private void addIngredientRow(VBox ingredientsBox) {
        HBox ingredientRow = new HBox(10);
        ingredientRow.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Zutat");
        nameField.setPrefWidth(120);

        TextField amountField = new TextField();
        amountField.setPromptText("Menge");
        amountField.setPrefWidth(80);

        ComboBox<Unit> unitComboBox = createEnumComboBox(localizedUnitMap);
        unitComboBox.setPromptText("Einheit");
        //unitComboBox.getSelectionModel().select(Unit.NONE);
        unitComboBox.setPrefWidth(80);

        ComboBox<Category> categoryComboBox = createEnumComboBox(localizedCategoryMap);
        categoryComboBox.setPromptText("Kategorie");
        //categoryComboBox.getSelectionModel().select(Category.NONE);
        categoryComboBox.setPrefWidth(100);

        TextField typeField = new TextField();
        typeField.setPromptText("Typ");
        typeField.setPrefWidth(100);

        TextField prepField = new TextField();
        prepField.setPromptText("Vorbereitung");
        prepField.setPrefWidth(120);

        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e -> ingredientsBox.getChildren().remove(ingredientRow));

        ingredientRow.getChildren().addAll(nameField, amountField, unitComboBox,
                categoryComboBox, typeField, prepField, removeButton);

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
                TextField amountF = (TextField) row.getChildren().get(1);
                ComboBox<Unit> unitF = (ComboBox<Unit>) row.getChildren().get(2);
                ComboBox<Category> categoryF = (ComboBox<Category>) row.getChildren().get(3);
                TextField typeF = (TextField) row.getChildren().get(4);
                TextField prepF = (TextField) row.getChildren().get(5);

                String ingName = nameF.getText().trim();
                if (!ingName.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountF.getText().trim());
                        RecipeIngredient ingredient = new RecipeIngredient(
                                ingName, unitF.getValue(), amount,
                                categoryF.getValue(), typeF.getText().trim(),
                                prepF.getText().trim());
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
        mealPlanner.saveRecipeBook();
        return true;
    }

    private void clearAddRecipeForm(TextField nameField, TextArea descriptionArea, VBox ingredientsBox) {
        nameField.clear();
        descriptionArea.clear();
        ingredientsBox.getChildren().clear();
        addIngredientRow(ingredientsBox);
    }


}
