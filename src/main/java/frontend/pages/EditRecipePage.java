package frontend.pages;

import frontend.AppState;
import frontend.NavigationButton;
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

public class EditRecipePage extends Page {
    private MealPlannerService mealPlanner;
    private AppState appState;
    private Recipe recipe;

    public EditRecipePage(Navigator navigator, MealPlannerService mealPlanner, AppState appState) {
        super(navigator);
        this.mealPlanner = mealPlanner;
        this.appState = appState;
    }

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Rezept bearbeiten");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        recipe = appState.getSelectedRecipe();

        TextField nameField = new TextField(recipe.getName());
        TextArea descriptionArea = new TextArea(recipe.getDescription());
        descriptionArea.setPrefRowCount(3);

        Label ingredientsLabel = new Label("Zutaten:");
        ingredientsLabel.setStyle("-fx-font-weight: bold;");

        VBox ingredientsBox = new VBox(10);
        ScrollPane ingredientsScroll = new ScrollPane(ingredientsBox);
        ingredientsScroll.setPrefHeight(300);

        for (RecipeIngredient ing : recipe.getIngredients()) {
            addIngredientRow(ingredientsBox, ing);
        }

        Button addIngredientButton = new Button("Zutat hinzufügen");
        addIngredientButton.setOnAction(e -> addIngredientRow(ingredientsBox, null));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Speichern");
        Button backButton = new NavigationButton("Zurück zu Rezepten", Route.RECIPE, navigator);

        saveButton.setOnAction(e -> {
            if (updateRecipe(nameField, descriptionArea, ingredientsBox, recipe)) {
                navigator.show(Route.RECIPE);
            }
        });

        buttonBox.getChildren().addAll(saveButton, backButton);

        root.getChildren().addAll(titleLabel, new Label("Name:"), nameField, new Label("Beschreibung:"), descriptionArea, ingredientsLabel, ingredientsScroll, addIngredientButton, buttonBox);

        return root;
    }

    private void addIngredientRow(VBox ingredientsBox, RecipeIngredient ingredient) {
        HBox ingredientRow = new HBox(10);
        ingredientRow.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPrefWidth(120);
        ComboBox<Unit> unitComboBox = createEnumComboBox(localizedUnitMap);
        unitComboBox.setPrefWidth(80);
        TextField amountField = new TextField();
        amountField.setPrefWidth(80);
        ComboBox<Category> categoryComboBox = createEnumComboBox(localizedCategoryMap);
        categoryComboBox.setPrefWidth(100);
        TextField typeField = new TextField();
        typeField.setPrefWidth(100);
        TextField prepField = new TextField();
        prepField.setPrefWidth(120);

        if (ingredient != null) {
            nameField.setText(ingredient.getName());
            unitComboBox.setValue(ingredient.getUnit());
            amountField.setText(String.valueOf(ingredient.getAmount()));
            categoryComboBox.setValue(ingredient.getCategory());
            typeField.setText(ingredient.getFoodType());
            prepField.setText(ingredient.getPreparation());
        } else {
            nameField.setPromptText("Zutat");
            unitComboBox.setPromptText("Einheit");
            amountField.setPromptText("Menge");
            categoryComboBox.setPromptText("Kategorie");
            typeField.setPromptText("Typ");
            prepField.setPromptText("Vorbereitung");
        }

        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e -> ingredientsBox.getChildren().remove(ingredientRow));

        ingredientRow.getChildren().addAll(nameField, amountField, unitComboBox, categoryComboBox, typeField, prepField, removeButton);

        ingredientsBox.getChildren().add(ingredientRow);
    }

    private boolean updateRecipe(TextField nameField, TextArea descriptionArea, VBox ingredientsBox, Recipe recipe) {
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
                        RecipeIngredient ingredient = new RecipeIngredient(ingName, unitF.getValue(), amount, categoryF.getValue(), typeF.getText().trim(), prepF.getText().trim());
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

        recipe.setName(name);
        recipe.setIngredients(ingredients);
        if (description != null) recipe.setDescription(description);
        mealPlanner.saveRecipeBook();
        return true;
    }
}
