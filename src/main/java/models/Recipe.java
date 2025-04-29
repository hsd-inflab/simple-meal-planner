package models;

import java.util.List;

public class Recipe {
    private String name;
    private List<RecipeIngredient> ingredients;

    public Recipe() {}

    public Recipe(String name, List<RecipeIngredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public String toString() {
        return name + ": " + ingredients;
    }
}
