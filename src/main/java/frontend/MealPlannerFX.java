package frontend;

import frontend.pages.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import models.*;
import services.MealPlannerService;
import services.RecipeAPIService;

import models.RecipeIngredient;

public class MealPlannerFX extends Application {
    private MealPlannerService mealPlanner;
    private Stage primaryStage;
    private RecipeAPIService recipeAPIService;


    private Navigator navigator;
    private AppState appState;
    // Scenes

    private Scene availableRecipesScene;
    private Scene mealPlansScene;
    private Scene addMealPlanScene;

    // Components für Datenaktualisierung



    private ListView<Recipe> availableRecipesListView;
    private ListView<String> mealPlansListView;




    private Locale locale = Locale.GERMAN;

    private Map<Category, String> localizedCategoryMap;
    private Map<Unit, String> localizedUnitMap;

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

        BorderPane root = new BorderPane();
        Page.setLocale(locale);
        navigator = new Navigator(root);
        appState = new AppState();
        importEnumMappings();

        navigator.register(Route.MAIN, new MainPage(navigator, primaryStage));
        navigator.register(Route.RECIPE, new RecipePage(navigator, mealPlanner, appState));
        navigator.register(Route.ADD_RECIPE, new AddRecipePage(navigator, mealPlanner));
        navigator.register(Route.EDIT_RECIPE, new EditRecipePage(navigator, mealPlanner, appState));
        navigator.register(Route.GENERATE_RECIPE, new GenerateRecipePage(navigator, mealPlanner, recipeAPIService));
        navigator.register(Route.PANTRY, new PantryPage(navigator, mealPlanner));
        navigator.register(Route.ADD_GROCERY, new AddGroceryPage(navigator, mealPlanner));

        primaryStage.setTitle("HSD MealPlanner");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);

        Scene scene = new Scene(root, 1000, 700);

        createAllScenes();
        primaryStage.setScene(scene);
        primaryStage.show();

        navigator.show(Route.MAIN);     //Show main Page
    }

    private void importEnumMappings() {
        localizedCategoryMap = Category.getLocalizedMap(locale);
        localizedUnitMap = Unit.getLocalizedMap(locale);
    }

    private void createAllScenes() {
        availableRecipesScene = createAvailableRecipesScene();
        mealPlansScene = createMealPlansScene();
        addMealPlanScene = createAddMealPlanScene();
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

        Button backButton = new Button("Zurück zu Speisepläne");
        backButton.setOnAction(e -> switchToScene(mealPlansScene));

        root.getChildren().addAll(titleLabel, availableRecipesListView, backButton);
        return new Scene(root, 1000, 700);
    }


// Neue Felder am Anfang der Klasse ergänzen:
    private GridPane mealPlansGrid;
    private TextArea mealPlanDetailsArea;

    // ============ MEAL PLANS SCENE ============
    private Scene createMealPlansScene() {
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

        Button addButton = new Button("Neuen Speiseplan hinzufügen");
        Button availableButton = new Button("Mögliche Rezepte anzeigen");
        Button backButton = new Button("Zurück zum Hauptmenü");

        addButton.setOnAction(e -> switchToScene(addMealPlanScene));
        availableButton.setOnAction(e -> {
            refreshAvailableRecipes();
            switchToScene(availableRecipesScene);
        });
        //backButton.setOnAction(e -> switchToScene(mainMenuScene)); commented bcs debug

        buttonBox.getChildren().addAll(addButton, availableButton, backButton);
        root.getChildren().addAll(titleLabel, scrollPane, mealPlanDetailsArea, buttonBox);

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

    // ============ UTILITY METHODS ============
    private void switchToScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    private void refreshRecipes() {
        //recipeListView.getItems().clear();
        //recipeListView.getItems().addAll(mealPlanner.getRecipeBook());        debug
        //recipeDetailsTextArea.setText("Wähle ein Rezept aus der Liste");
    }



    private void refreshAvailableRecipes() {
        availableRecipesListView.getItems().clear();
        List<Recipe> available = mealPlanner.getAvailableRecipes();
        availableRecipesListView.getItems().addAll(available);
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

}
