package hsd.inflab.smp.frontend;

import hsd.inflab.smp.entity.Recipe;

/**
 * Used for exchanging values and states between Pages
 */
public class AppState {
    private Recipe selectedRecipe;

    public Recipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }
}
