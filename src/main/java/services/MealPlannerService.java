package services;

import models.Ingredient;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

import java.util.*;

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<Ingredient> pantry;

    public void startCLILoop() {
        Scanner scanner = new Scanner(System.in);
        loadPantry();
        loadRecipeBook();
        System.out.println("Welcome to the HSD:MealPlanner.");
        while(true) {
            System.out.println("1. Show recipes");
            System.out.println("2. show ingredients");
            System.out.println("Input menu point:");
            int input = scanner.nextInt();
            if (input == 2)
                System.out.println(pantry.toString());
            if (input == 1)
                System.out.println(recipeBook.toString());
        }
    }

    private void loadPantry() {
        pantry = new ArrayList<>();
        pantry.add(new PantryItem(
                "Egg",             // name
                "unit",            // unit
                1.0,               // amount
                "Dairy",           // category
                new Date(),        // expirationDate
                new Date(),        // purchaseDate
                "BioFarm",         // brand
                0.29               // price
        ));
        //replace with wrapper method for importing from JSON
    }

    private void loadRecipeBook() {
        recipeBook = new ArrayList<>();
        List<RecipeIngredient> ingredientsForRecipe = new ArrayList<>();
        ingredientsForRecipe.add(new RecipeIngredient("m-sized Hen's Egg", "unit", 2.0, "Egg" , "Scrambled Egg", "raw"));
        recipeBook.add(new Recipe("Omelett", ingredientsForRecipe));
        //replace with wrapper method for importiung from json
    }
}
