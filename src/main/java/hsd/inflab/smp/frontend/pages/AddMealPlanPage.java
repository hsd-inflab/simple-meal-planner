package hsd.inflab.smp.frontend.pages;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.enums.Route;
import hsd.inflab.smp.frontend.Navigator;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.util.DateFormatUtil;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddMealPlanPage extends Page {
    private final MealPlannerService mealPlanner;
    private final String ERROR_TITLE = "Fehler"; // NOPMD

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
        weekdayBox.getItems().addAll("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag");

        // Meal selection
        Label mealsLabel = new Label("Mahlzeiten:");
        mealsLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<RecipeDto> breakfastBox = new ComboBox<>();
        ComboBox<RecipeDto> lunchBox = new ComboBox<>();
        ComboBox<RecipeDto> dinnerBox = new ComboBox<>();

        // Populate ComboBoxes with available recipes
        breakfastBox.getItems().addAll(mealPlanner.recipeService.getRecipeBook());
        lunchBox.getItems().addAll(mealPlanner.recipeService.getRecipeBook());
        dinnerBox.getItems().addAll(mealPlanner.recipeService.getRecipeBook());

        Label personsLabel = new Label("Anzahl Personen pro Mahlzeit:");
        personsLabel.setStyle("-fx-font-weight: bold;");

        Spinner<Integer> breakfastPersonsSpinner = new Spinner<>(1, 20, 1);
        Spinner<Integer> lunchPersonsSpinner = new Spinner<>(1, 20, 1);
        Spinner<Integer> dinnerPersonsSpinner = new Spinner<>(1, 20, 1);

        HBox breakfastRow =
                new HBox(10, new Label("Frühstück:"), breakfastBox, new Label("Personen:"), breakfastPersonsSpinner);
        HBox lunchRow = new HBox(10, new Label("Mittagessen:"), lunchBox, new Label("Personen:"), lunchPersonsSpinner);
        HBox dinnerRow =
                new HBox(10, new Label("Abendessen:"), dinnerBox, new Label("Personen:"), dinnerPersonsSpinner);

        breakfastRow.setAlignment(Pos.CENTER_LEFT);
        lunchRow.setAlignment(Pos.CENTER_LEFT);
        dinnerRow.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Speichern");
        Button backButton = new Button("Zurück zu Speiseplänen");

        saveButton.setOnAction(e -> {
            if (saveMealPlan(
                    dateGroup,
                    dateField,
                    daysField,
                    weekdayBox,
                    breakfastBox,
                    lunchBox,
                    dinnerBox,
                    breakfastPersonsSpinner.getValue(),
                    lunchPersonsSpinner.getValue(),
                    dinnerPersonsSpinner.getValue())) {
                clearMealPlanForm(dateField, daysField, weekdayBox, breakfastBox, lunchBox, dinnerBox);
                navigator.show(Route.MEALPLAN);
            }
        });
        backButton.setOnAction(e -> {
            clearMealPlanForm(dateField, daysField, weekdayBox, breakfastBox, lunchBox, dinnerBox);
            navigator.show(Route.MEALPLAN);
        });

        buttonBox.getChildren().addAll(saveButton, backButton);

        root.getChildren()
                .addAll(
                        titleLabel,
                        dateLabel,
                        dateRadio,
                        dateField,
                        daysRadio,
                        daysField,
                        weekdayRadio,
                        weekdayBox,
                        mealsLabel,
                        breakfastRow,
                        lunchRow,
                        dinnerRow,
                        buttonBox);

        return root;
    }

    private boolean saveMealPlan(
            ToggleGroup dateGroup,
            TextField dateField,
            TextField daysField,
            ComboBox<String> weekdayBox,
            ComboBox<RecipeDto> breakfastBox,
            ComboBox<RecipeDto> lunchBox,
            ComboBox<RecipeDto> dinnerBox,
            int breakfastPersons,
            int lunchPersons,
            int dinnerPersons) {
        try {
            LocalDate date = null;
            RadioButton selected = (RadioButton) dateGroup.getSelectedToggle();

            if (selected.getText().contains("Datum")) {
                date = DateFormatUtil.parseGermanDate(dateField.getText());
            } else if (selected.getText().contains("Tage")) {
                int days = Integer.parseInt(daysField.getText().trim());
                date = LocalDate.now().plusDays(days);
            } else if (selected.getText().contains("Wochentag")) {
                String weekday = weekdayBox.getValue();
                if (weekday == null) {
                    showError(ERROR_TITLE, "Bitte wählen Sie einen Wochentag.");
                    return false;
                }
                date = getNextWeekday(weekday);
            }

            if (date == null || date.isBefore(LocalDate.now())) {
                showError(ERROR_TITLE, "Ungültiges Datum oder Datum liegt in der Vergangenheit.");
                return false;
            }

            DailyMealDto dailyMeal = new DailyMealDto(
                    null,
                    date,
                    breakfastBox.getValue(),
                    lunchBox.getValue(),
                    dinnerBox.getValue(),
                    breakfastPersons,
                    lunchPersons,
                    dinnerPersons);

            mealPlanner.dailyMealService.saveOrUpdateDailyMeal(dailyMeal);
            return true;

        } catch (NumberFormatException e) {
            showError(ERROR_TITLE, "Tage müssen eine ganze Zahl sein: " + e.getMessage());
            return false;
        }
    }

    private LocalDate getNextWeekday(String weekday) {
        LocalDate date = LocalDate.now();
        int targetDay =
                switch (weekday) {
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

    private void clearMealPlanForm(
            TextField dateField, TextField daysField, ComboBox<String> weekdayBox, ComboBox<RecipeDto>... comboBoxes) {
        dateField.clear();
        daysField.clear();
        weekdayBox.setValue(null);
        for (ComboBox<RecipeDto> box : comboBoxes) {
            box.setValue(null);
        }
    }
}
