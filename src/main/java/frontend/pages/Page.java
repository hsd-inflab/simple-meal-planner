package frontend.pages;

import frontend.Navigator;
import javafx.scene.Parent;

public abstract class Page {
    protected final Navigator navigator;

    protected Page (Navigator navigator) {
        this.navigator = navigator;
    }

    public void onHide() {}
    public void onShow() {}

    public abstract Parent getView();
}
