package frontend.pages;

import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import models.Category;
import models.PantryItem;
import models.Route;
import models.Unit;

import services.MealPlannerService;

import java.time.LocalDate;

public class AddGroceryPage extends Page {
    private MealPlannerService mealPlanner;

    public AddGroceryPage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
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
        ComboBox<Unit> unitComboBox = createEnumComboBox(Unit.getLocalizedMap(locale));
        //TextField unitField = new TextField();
        TextField amountField = new TextField();
        ComboBox<Category> categoryComboBox = createEnumComboBox(Category.getLocalizedMap(locale));
        //TextField categoryField = new TextField();
        TextField daysField = new TextField();
        TextField brandField = new TextField();
        TextField priceField = new TextField();

        // Labels and fields
        root.add(new Label("Name:"), 0, 1);
        root.add(nameField, 1, 1);
        root.add(new Label("Einheit:"), 0, 2);
        root.add(unitComboBox, 1, 2);
        root.add(new Label("Menge:"), 0, 3);
        root.add(amountField, 1, 3);
        root.add(new Label("Kategorie:"), 0, 4);
        root.add(categoryComboBox, 1, 4);
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
            if (saveGrocery(nameField, unitComboBox, amountField, categoryComboBox,
                    daysField, brandField, priceField)) {
                clearGroceryForm(nameField, amountField,
                        daysField, brandField, priceField);
                navigator.show(Route.PANTRY);
            }
        });
        backButton.setOnAction(e -> {
            clearGroceryForm(nameField, amountField,
                    daysField, brandField, priceField);
            navigator.show(Route.PANTRY);
        });

        buttonBox.getChildren().addAll(saveButton, backButton);
        root.add(buttonBox, 0, 8, 2, 1);

        return root;
    }

    private boolean saveGrocery(TextField nameField, ComboBox<Unit> unitComboBox, TextField amountField,
                                ComboBox<Category> categoryComboBox, TextField daysField, TextField brandField,
                                TextField priceField) {
        try {
            String name = nameField.getText().trim();
            Unit unit = unitComboBox.getValue();
            double amount = Double.parseDouble(amountField.getText().trim());
            Category category = categoryComboBox.getValue();
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
}
