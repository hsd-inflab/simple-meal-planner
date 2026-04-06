package hsd.inflab.smp.frontend;

import javafx.scene.control.Button;
import hsd.inflab.smp.model.Route;

/**
 * Subimplementation for JavaFX Button with integrated support for the navigator
 */
public class NavigationButton extends Button {

    public NavigationButton(String label, Route target, Navigator navigator) {
        super(label);
        setOnAction(e -> navigator.show(target));
    }
}
