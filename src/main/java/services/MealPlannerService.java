package services;

import models.Ingredient;
import models.Recipe;
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
            if (input == 1)
                System.out.print(pantry.toString());
            if (input == 2)
                System.out.print(recipeBook.toString());
        }
    }

    private void loadPantry() {
        pantry = new ArrayList<>();
        pantry.add(new Ingredient("Egg", "distinct", 10.0, new Date()));
        //replace with wrapper method for importing from JSON
    }

    private void loadRecipeBook() {
        recipeBook = new ArrayList<>();
        Map<Ingredient, Double> ingredientsForRecipe = new HashMap<>();
        ingredientsForRecipe.put(new Ingredient("Egg"), 2.0);
        recipeBook.add(new Recipe("Omelett", ingredientsForRecipe));
        //replace with wrapper method for importiung from json
    }
}
