package frontend.pages;

import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import models.DailyMeal;
import models.Recipe;
import models.RecipeIngredient;
import models.Route;

import services.MealPlannerService;

import java.time.LocalDate;

public class AddMealPlanPage extends Page {
    private MealPlannerService mealPlanner;

    public AddMealPlanPage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Neuen Speiseplan hinzufügen");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Date input section
        Label dateLabel = new Label("Datum auswählen:");
        dateLabel.setStyle("-fx-font-weight: bold;");

        ToggleGroup dateGroup = new ToggleGroup();
        RadioButton dateRadio = new RadioButton("Datum (TT-MM-JJJJ)");
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

        HBox breakfastRow = new HBox(10, new Label("Frühstück:"), breakfastBox, new Label("Personen:"),
                breakfastPersonsSpinner);
        HBox lunchRow = new HBox(10, new Label("Mittagessen:"), lunchBox, new Label("Personen:"), lunchPersonsSpinner);
        HBox dinnerRow = new HBox(10, new Label("Abendessen:"), dinnerBox, new Label("Personen:"),
                dinnerPersonsSpinner);

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
                    breakfastPersonsSpinner.getValue(), lunchPersonsSpinner.getValue(),
                    dinnerPersonsSpinner.getValue())) {
                clearMealPlanForm(dateField, daysField, weekdayBox,
                        breakfastBox, lunchBox, dinnerBox);
                navigator.show(Route.MEALPLAN);
            }
        });
        backButton.setOnAction(e -> {
            clearMealPlanForm(dateField, daysField, weekdayBox,
                    breakfastBox, lunchBox, dinnerBox);
            navigator.show(Route.MEALPLAN);
        });

        buttonBox.getChildren().addAll(saveButton, backButton);

        root.getChildren().addAll(titleLabel, dateLabel, dateRadio, dateField,
                daysRadio, daysField, weekdayRadio, weekdayBox,
                mealsLabel,
                breakfastRow,
                lunchRow,
                dinnerRow,
                buttonBox);

        return root;
    }

    private boolean saveMealPlan(ToggleGroup dateGroup, TextField dateField, TextField daysField,
                                 ComboBox<String> weekdayBox, ComboBox<Recipe> breakfastBox,
                                 ComboBox<Recipe> lunchBox, ComboBox<Recipe> dinnerBox,
                                 int breakfastPersons, int lunchPersons, int dinnerPersons) {
        RecipeIngredient ingredient = new RecipeIngredient();
        try {
            LocalDate date = null;
            RadioButton selected = (RadioButton) dateGroup.getSelectedToggle();

            if (selected.getText().contains("Datum")) {
                date = ingredient.parseGermanDate(dateField.getText());
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
}
