package frontend.pages;

import frontend.NavigationButton;
import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import models.DailyMeal;
import models.Recipe;
import models.RecipeIngredient;
import models.Route;

import services.MealPlannerService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.TreeMap;

public class MealPlanPage extends Page {
    private MealPlannerService mealPlanner;

    private GridPane mealPlansGrid;
    private TextArea mealPlanDetailsArea;

    public MealPlanPage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Speisepläne ab " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        mealPlansGrid = new GridPane();
        mealPlansGrid.setHgap(15);
        mealPlansGrid.setVgap(10);

        ScrollPane scrollPane = new ScrollPane(mealPlansGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        mealPlanDetailsArea = new TextArea("Klicke auf ein Rezept, um Details zu sehen.");
        mealPlanDetailsArea.setEditable(false);
        mealPlanDetailsArea.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new NavigationButton("Neuen Speiseplan hinzufügen", Route.ADD_MEALPLAN, navigator);
        Button availableButton = new NavigationButton("Mögliche Rezepte anzeigen", Route.AVAILABLE_RECIPES, navigator);
        Button backButton = new NavigationButton("Zurück zum Hauptmenü", Route.MAIN, navigator);

        buttonBox.getChildren().addAll(addButton, availableButton, backButton);
        root.getChildren().addAll(titleLabel, scrollPane, mealPlanDetailsArea, buttonBox);

        refreshMealPlans();

        return root;
    }

    private void refreshMealPlans() {
        mealPlansGrid.getChildren().clear();
        Map<LocalDate, DailyMeal> plans = new TreeMap<>(mealPlanner.getDailyMealPlans()); // chronologisch sortiert

        if (plans.isEmpty()) return;

        LocalDate start = plans.keySet().iterator().next();
        LocalDate end = plans.keySet().stream().max(LocalDate::compareTo).orElse(start);

        int row = 1;

        // Header
        mealPlansGrid.add(new Label("Datum"), 0, 0);
        mealPlansGrid.add(new Label("Frühstück"), 1, 0);
        mealPlansGrid.add(new Label("Mittagessen"), 2, 0);
        mealPlansGrid.add(new Label("Abendessen"), 3, 0);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DailyMeal dailyMeal = plans.get(date);

            mealPlansGrid.add(new Label(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))), 0, row);

            addMealButton(dailyMeal != null ? dailyMeal.getBreakfast() : null,
                    dailyMeal != null ? dailyMeal.getNumberOfPersonsBreakfast() : 0, 1, row);
            addMealButton(dailyMeal != null ? dailyMeal.getLunch() : null,
                    dailyMeal != null ? dailyMeal.getNumberOfPersonsLunch() : 0, 2, row);
            addMealButton(dailyMeal != null ? dailyMeal.getDinner() : null,
                    dailyMeal != null ? dailyMeal.getNumberOfPersonsDinner() : 0, 3, row);

            row++;
        }
    }

    private void addMealButton(Recipe recipe, int persons, int col, int row) {
        if (recipe == null) {
            mealPlansGrid.add(new Label("-"), col, row);
        } else {
            Button mealButton = new Button(recipe.getName() + " (" + persons + ")");
            //mealButton.setOnAction(e -> showRecipeDetails(recipe, 1)); debug
            mealButton.setOnAction(e -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Rezept: ").append(recipe.getName()).append("\n")
                        .append("Beschreibung: ").append(recipe.getDescription()).append("\n\nZutaten:\n");
                for (RecipeIngredient ing : recipe.getIngredients()) {
                    sb.append("- ").append(ing.getName()).append(" ").append(ing.getAmount())
                            .append(" ").append(localizedUnitMap.get(ing.getUnit())).append("\n");
                }
                mealPlanDetailsArea.setText(sb.toString());
            });
            mealPlansGrid.add(mealButton, col, row);
        }
    }

    @Override
    public void onShow() {
        refreshMealPlans();
    }
}
