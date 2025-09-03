package models;

import java.util.List;


public class Recipe {
    private String name;
     private String description;
    private List<RecipeIngredient> ingredientsPerPerson;

    public Recipe() {}

    public Recipe(String name, List<RecipeIngredient> ingredients) {
        this.name = name;
        this.ingredientsPerPerson = ingredients;
    }
  
    public Recipe (String name, String description, List<RecipeIngredient> ingredientsPerPerson)  {
        this.name = name;
        this.description = description;
        this.ingredientsPerPerson = ingredientsPerPerson;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredientsPerPerson;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredientsPerPerson = ingredients;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredientsPerPerson.add(ingredient);
    }

    public void printDetails() {
        System.out.println("Recipe: " + name);
        System.out.println("Description: " + description);
        System.out.println("Ingredients per Person:");
        for (RecipeIngredient ri : ingredientsPerPerson) {
            ri.printDetails();
            System.out.println();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
