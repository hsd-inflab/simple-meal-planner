package hsd.inflab.smp.frontend;

import hsd.inflab.smp.enums.Route;
import hsd.inflab.smp.frontend.pages.Page;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.BorderPane;

/**
 * Used for navigation between the frontend pages, buttons implement show(), frontend main class uses register()
 */
public class Navigator {
    private final BorderPane root;
    private final Map<Route, Page> routes = new HashMap<>();
    private Page currentPage;

    public Navigator(BorderPane root) {
        this.root = root;
    }

    public void register(Route name, Page page) {
        routes.put(name, page);
    }

    public void show(Route name) {
        Page next = routes.get(name);

        if (next == null) {
            System.out.println("Diese Route ist nicht angelegt!");
            return;
        }

        if (currentPage != null) {
            currentPage.onHide();
        }

        currentPage = next;
        root.setCenter(next.getView());
        currentPage.onShow();
    }
}
