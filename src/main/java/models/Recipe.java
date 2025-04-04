package models;

import java.util.List;

public class Recipe {
    private String name;
    private List<RecipeIngredient> ingredientsPerPerson;

    public Recipe (String name, List<RecipeIngredient> ingredientsPerPerson)  {
        this.name = name;
        this.ingredientsPerPerson = ingredientsPerPerson;
    }

    @Override
    public String toString() {
        return name + ingredientsPerPerson;
    }
}
