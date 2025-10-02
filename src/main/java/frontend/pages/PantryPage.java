package frontend.pages;

import frontend.NavigationButton;
import frontend.Navigator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import models.PantryItem;
import models.Route;

import services.MealPlannerService;

public class PantryPage extends Page {
    private MealPlannerService mealPlanner;

    private ListView<PantryItem> pantryListView;
    private Label pantryDetailsLabel;

    public PantryPage(Navigator navigator, MealPlannerService mealPlanner) {
        super(navigator);
        this.mealPlanner = mealPlanner;
    }

    @Override
    public Parent getView() {
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

        Button addButton = new NavigationButton("Lebensmittel hinzufügen", Route.ADD_GROCERY, navigator);
        Button backButton = new NavigationButton("Zurück zum Hauptmenü", Route.MAIN, navigator);

        buttonBox.getChildren().addAll(addButton, backButton);
        root.setBottom(buttonBox);

        return root;
    }

    private void showPantryDetails(PantryItem item) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(item.getName()).append("\n");
        details.append("Menge: ").append(item.getAmount()).append(" ").append(localizedUnitMap.get(item.getUnit())).append("\n");
        details.append("Kategorie: ").append(localizedCategoryMap.get(item.getCategory())).append("\n");
        details.append("Marke: ").append(item.getBrand()).append("\n");
        details.append("Preis: ").append(item.getPrice()).append("€\n");
        details.append("Gekauft am: ").append(item.formatAsGermanDate(item.getPurchaseDate())).append("\n");
        details.append("Verfällt am: ").append(item.formatAsGermanDate(item.getExpirationDate()));


        pantryDetailsLabel.setText(details.toString());
    }

    private void refreshPantry() {
        pantryListView.getItems().clear();
        pantryListView.getItems().addAll(mealPlanner.getPantry());
        pantryDetailsLabel.setText("Wähle ein Item aus der Liste");
    }

    @Override
    public void onShow() {
        refreshPantry();
    }
}
