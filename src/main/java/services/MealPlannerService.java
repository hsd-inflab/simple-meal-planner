package services;

import models.Ingredient;
import models.PantryItem;
import models.Recipe;
import models.RecipeIngredient;

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
        while(true) {
            System.out.println("1. Show recipes");
            System.out.println("11. add recipes");
            System.out.println("2. show pantry");
            System.out.println("21. add groceries");
            System.out.println("0. exit menu");
            //System.out.println("Input menu point:");
            int input = readInt(scanner, "Input menu point:");
            //scanner.nextLine();                         //consume line break
            if (input == 2)
                printPantryContents();
            if (input == 21)
                addGroceries(scanner);
            if (input == 1)
                printAllRecipes();
            if (input == 11)
                addRecipe(scanner);
            if (input == 0)
                break;
        }

        savePantry();
        saveRecipeBook();
        scanner.close();
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
        pantry = new ArrayList<>();
        pantry.add(new PantryItem(
                "Egg",             // name
                "unit",            // unit
                1.0,               // amount
                "Dairy",           // category
                LocalDate.now(),        // expirationDate
                LocalDate.now(),        // purchaseDate
                "BioFarm",         // brand
                0.29               // price
        ));
        //replace with wrapper method for importing from JSON
    }

    private void savePantry() {
        //insert jackson wrapper method
    }

    private void loadRecipeBook() {
        recipeBook = new ArrayList<>();
        List<RecipeIngredient> ingredientsForRecipe = new ArrayList<>();
        ingredientsForRecipe.add(new RecipeIngredient("m-sized Hen's Egg", "unit", 2.0, "Egg" , "Scrambled Egg", "raw"));
        recipeBook.add(new Recipe("Omelett", "scramble egg. put into pan. turn omelett", ingredientsForRecipe));
        //replace with wrapper method for importiung from json
    }

    private void saveRecipeBook() {
        //insert jackson wrapper method
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
