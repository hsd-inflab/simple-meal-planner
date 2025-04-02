package models;

import java.util.Map;

public class Recipe {
    private String name;
    private Map<Ingredient, Double> mealIngredientsPerPerson;

    public Recipe (String name, Map<Ingredient, Double> mealIngredientsPerPerson)  {
        this.name = name;
        this.mealIngredientsPerPerson = mealIngredientsPerPerson;
    }

    @Override
    public String toString() {
        return name + mealIngredientsPerPerson;
    }
}
