package frontend.pages;

import frontend.Navigator;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import models.Category;
import models.Unit;

import java.util.Locale;
import java.util.Map;

/**
 * page superclass
 */

public abstract class Page {
    protected final Navigator navigator;

    protected static Locale locale;
    protected Map<Category, String> localizedCategoryMap;
    protected Map<Unit, String> localizedUnitMap;

    protected Page(Navigator navigator) {
        this.navigator = navigator;
        localizedCategoryMap = Category.getLocalizedMap(locale);
        localizedUnitMap = Unit.getLocalizedMap(locale);
    }

    public void onHide() {
    }

    public void onShow() {
    }

    public abstract Parent getView();

    public static void setLocale(Locale locale) {
        Page.locale = locale;
    }

    protected <E extends Enum<E>> ComboBox<E> createEnumComboBox(Map<E, String> localizedMap) {
        ComboBox<E> comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList(localizedMap.keySet()));

        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localizedMap.get(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localizedMap.get(item));
            }
        });

        return comboBox;
    }

    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
