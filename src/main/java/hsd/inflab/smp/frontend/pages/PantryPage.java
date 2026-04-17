package hsd.inflab.smp.frontend.pages;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Route;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.frontend.NavigationButton;
import hsd.inflab.smp.frontend.Navigator;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.util.DateFormatUtil;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PantryPage extends Page {
    private final MealPlannerService mealPlanner;

    private ListView<PantryItemDto> pantryListView;
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
            PantryItemDto selected = pantryListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pantryDetailsLabel.setText(getDetails(locale, selected));
            }
        });

        leftBox.getChildren().addAll(listLabel, pantryListView);
        root.setLeft(leftBox);

        // Center: Item Details
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(0, 10, 0, 10));
        Label detailsLabel = new Label("Lebensmitteldetails:");
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

    private void refreshPantry() {
        pantryListView.getItems().clear();
        pantryListView.getItems().addAll(mealPlanner.pantryService.getPantry());
        pantryDetailsLabel.setText("Wähle ein Item aus der Liste");
    }

    @Override
    public void onShow() {
        refreshPantry();
    }

    public String getDetails(Locale locale, PantryItemDto item) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(item.name()).append("\n");
        details.append("Menge: ")
                .append(item.amount())
                .append(" ")
                .append(Unit.getLocalizedMap(locale).get(item.unit()))
                .append("\n");
        details.append("Kategorie: ")
                .append(Category.getLocalizedMap(locale).get(item.category()))
                .append("\n");
        details.append("Marke: ").append(item.brand()).append("\n");
        details.append("Preis: ").append(item.price()).append("€\n");
        details.append("Gekauft am: ")
                .append(DateFormatUtil.formatAsGermanDate(item.purchaseDate()))
                .append("\n");
        details.append("Verfällt am: ").append(DateFormatUtil.formatAsGermanDate(item.expirationDate()));

        return details.toString();
    }
}
