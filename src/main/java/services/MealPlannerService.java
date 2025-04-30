package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Ingredient;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.time.LocalDate;

public class MealPlannerService {
    private List<Recipe> recipeBook;
    private List<PantryItem> pantry;

    public void startCLILoop() {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);                           // enables entering doubles with . instead of ,
        loadPantry();
        loadRecipeBook();
        System.out.println("Welcome to the HSD:MealPlanner.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {     //this thread runs after the program terminates
            savePantry();
            saveRecipeBook();
            scanner.close();
            System.out.println("mealplanner gracefully terminated.");
        }));

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show recipes");
            System.out.println("11. add recipes");
            System.out.println("2. Show pantry");
            System.out.println("21. add groceries");
            System.out.println("3. Show possible recipes");
            System.out.println("0. Exit");
            System.out.print("Input menu point: ");
            try {
                int input = Integer.parseInt(scanner.nextLine());
                switch (input) {
                    case 1 -> printAllRecipes();
                    case 11 -> addRecipe(scanner);
                    case 2 -> printPantryContents();
                    case 21 -> addGroceries(scanner);
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

    public void printAllRecipes() {
        if (recipeBook.isEmpty()) {
            System.out.println("Keine Rezepte vorhanden.");
            return;
        }
        System.out.println("=== Rezeptbuch ===");
        for (Recipe recipe : recipeBook) {
            recipe.printDetails();
            System.out.println("-------------------------");
        }
    }

    public void printPantryContents() {
        if (pantry.isEmpty()) {
            System.out.println("Vorratskammer ist leer.");
            return;
        }
        System.out.println("=== Vorratskammer ===");
        for (PantryItem item : pantry) {
            item.printDetails();
            System.out.println("-------------------------");
        }
    }

    private void addRecipe(Scanner scanner) {
        String recipeName, ingredient, description, unit, category, foodType, preparation;
        double amountPerPerson;

        System.out.println("Enter The name of the recipe:");
        recipeName = scanner.nextLine();
        //scanner.nextLine();
        System.out.println("Enter The description on how to cook this recipe:");
        description = scanner.nextLine();

        Recipe recipe = new Recipe(recipeName, description, new ArrayList<>());

        while (true) {
            System.out.println("Enter ingredient or type exit to save recipe:");
            String input = scanner.next();

            if(input.equals("exit"))
                break;

            ingredient = input;
            System.out.println("Enter the unit for this ingredient:");
            unit = scanner.nextLine();
            //System.out.println("Enter the amount per person:");
            amountPerPerson = readDouble(scanner, "Enter the amount per person:");
            System.out.println("Enter the food category (meat, vegetable, dairy, etc.):");
            category = scanner.nextLine();
            //System.out.println("Enter the food Type:");
            foodType = "deprecated";
            System.out.println("Enter the needed preparation of ingredient (sliced, scrambled, etc.) :");
            preparation = scanner.nextLine();

            RecipeIngredient recipeIngredient = new RecipeIngredient(ingredient, unit, amountPerPerson, category, foodType, preparation);
            recipe.addIngredient(recipeIngredient);
        }

        recipeBook.add(recipe);

    }

    private void addGroceries(Scanner scanner) {
        String name, unit, category, brand;
        double amount, price;
        LocalDate purchaseDate;
        int daysTillExpiration;

        System.out.println("Enter the name of Grocery:");
        name = scanner.nextLine();
        System.out.println("unit of measurement (unit, g, kg, teaspoon, tablespoon, L, mL):");
        unit = scanner.nextLine();
        //System.out.println("amount:");
        amount = readDouble(scanner, "amount:");
        System.out.println("food category (meat, vegetable, fruit):");
        category = scanner.nextLine();
        //System.out.println("days until expiration:");
        daysTillExpiration = readInt(scanner, "days until expiration:");
        System.out.println("Product brand:");
        brand = scanner.nextLine();
        //System.out.println("price:");
        price = readDouble(scanner, "price:");

        purchaseDate = LocalDate.now();
        LocalDate expirationDate = purchaseDate.plusDays(daysTillExpiration);


        PantryItem item = new PantryItem(name, unit, amount, category, expirationDate, purchaseDate, brand, price);
        pantry.add(item);
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

    private void savePantry() {
        //insert jackson wrapper method
        System.out.println("pantry saved.");
    }

    private void saveRecipeBook() {
        //insert jackson wrapper method
        System.out.println("recipe book saved.");
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
                Optional<PantryItem> matchingItem = pantry.stream()
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

    private double readDouble(Scanner scanner, String text) {
        double input;
        //boolean success = false;
        while(true){
            System.out.println(text);
            try {
                input = scanner.nextDouble();
                return input;
            }
            catch (Exception e) {
                System.out.println("That was not a number. Try again.");
            }
            finally {
                scanner.nextLine();
            }
        }
    }

    private int readInt(Scanner scanner, String text) {
        int input;
        //boolean success = false;
        while(true){
            System.out.println(text);
            try {
                input = scanner.nextInt();
                return input;
            }
            catch (Exception e) {
                System.out.println("That was not an Integer. Try again.");
            }
            finally {
                scanner.nextLine();
            }
        }
    }
}
