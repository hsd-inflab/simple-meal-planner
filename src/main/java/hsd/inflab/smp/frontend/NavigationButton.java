package hsd.inflab.smp.frontend;

import hsd.inflab.smp.enums.Route;
import javafx.scene.control.Button;

/**
 * Subimplementation for JavaFX Button with integrated support for the navigator
 */
public class NavigationButton extends Button {

    public NavigationButton(String label, Route target, Navigator navigator) {
        super(label);
        setOnAction(e -> navigator.show(target));
    }
}
