package hsd.inflab.smp.frontend;

import hsd.inflab.smp.dto.RecipeDto;

/**
 * Used for exchanging values and states between Pages
 */
public class AppState {
    private RecipeDto selectedRecipe;

    public RecipeDto getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(RecipeDto selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }
}
