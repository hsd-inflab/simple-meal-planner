package frontend;

import models.Recipe;

public class AppState {
    private Recipe selectedRecipe;


    public Recipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }
}
