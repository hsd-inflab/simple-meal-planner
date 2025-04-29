package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Ingredient;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<Ingredient> pantry;

    public void startCLILoop() {
        Scanner scanner = new Scanner(System.in);
        loadPantry();
        loadRecipeBook();
        System.out.println("Welcome to the HSD:MealPlanner.");
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show recipes");
            System.out.println("2. Show ingredients");
            System.out.println("3. Show possible recipes");
            System.out.println("0. Exit");
            System.out.print("Input menu point: ");
            try {
                int input = Integer.parseInt(scanner.nextLine());
                switch (input) {
                    case 1 -> System.out.println(recipeBook);
                    case 2 -> System.out.println(pantry);
                    case 3 -> {
                        List<Recipe> possibleRecipes = getAvailableRecipes();
                        if (possibleRecipes.isEmpty()) {
                            System.out.println("No recipes can be made with the current pantry items.");
                        } else {
                            System.out.println("You can make the following recipes:");
                            possibleRecipes.forEach(recipe -> System.out.println(recipe.getName()));
                        }
                    }

                    case 0 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid input.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private void loadPantry() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pantry = objectMapper.readValue(
                    new File("pantry.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PantryItem.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading pantry: " + e.getMessage());
            pantry = new ArrayList<>();
        }
    }

    private void loadRecipeBook() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            recipeBook = objectMapper.readValue(
                    new File("recipebook.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Recipe.class)
            );
        } catch (IOException e) {
            System.out.println("Error loading recipe book: " + e.getMessage());
            recipeBook = new ArrayList<>();
        }
    }

    public List<Recipe> getAvailableRecipes() {
        List<Recipe> availableRecipes = new ArrayList<>();

        for (Recipe recipe : recipeBook) {
            boolean canMake = true;

            // Durchlaufe alle Zutaten des Rezepts
            for (RecipeIngredient recipeIng : recipe.getIngredients()) {
                // Suche nach der Zutat in der Pantry
                Optional<Ingredient> matchingItem = pantry.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(recipeIng.getName()))  // Vergleiche die Namen der Zutaten
                        .findFirst();  // Finde das erste PantryItem, das der Zutat entspricht

                // Prüfe, ob die Zutat in der Pantry vorhanden ist und ob die Menge ausreicht
                if (matchingItem.isEmpty() || matchingItem.get().getAmount() < recipeIng.getAmount()) {
                    canMake = false;  // Rezept kann nicht gemacht werden, da Zutat fehlt oder Menge nicht ausreicht
                    break;  // Schleife abbrechen, da es nicht mehr möglich ist, das Rezept zu machen
                }
            }

            // Wenn das Rezept mit den Zutaten zubereitet werden kann, füge es zur Liste der verfügbaren Rezepte hinzu
            if (canMake) {
                availableRecipes.add(recipe);
            }
        }

        return availableRecipes;
    }
}
