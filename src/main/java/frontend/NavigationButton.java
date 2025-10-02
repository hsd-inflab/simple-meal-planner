package frontend;

import javafx.scene.control.Button;
import models.Route;

public class NavigationButton extends Button {

    public NavigationButton(String label, Route target, Navigator navigator) {
        super(label);
        setOnAction(e -> navigator.show(target));
    }
}
