package frontend;

import javafx.scene.Parent;

public abstract class Page {
    protected final Navigator navigator;

    protected Page (Navigator navigator) {
        this.navigator = navigator;
    }

    protected void onHide() {}
    protected void onShow() {}

    public abstract Parent getView();
}
