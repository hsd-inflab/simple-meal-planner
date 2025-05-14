package services;

import com.fasterxml.jackson.databind.ObjectMapper;

import models.DailyMeal;
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
    private Map<LocalDate, DailyMeal> dailyMealPlans = new TreeMap<>();
    
    public void startCLILoop() {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);                           // enables entering doubles with . instead of ,
        loadPantry();
        loadRecipeBook();
        loadMealPlans();
        System.out.println("Welcome to the HSD:MealPlanner.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {     //this thread runs after the program terminates
            savePantry();
            saveRecipeBook();
            saveMealPlans();
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
            System.out.println("4. Show meal plans"); 
            System.out.println("41. add meal plans");
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
                    case 4 -> printMealPlans();
                    case 41 -> {
                        addMealPlan(scanner);
                        
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
    
    private void loadMealPlans() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'loadMealPlans'");
    }


    private void addMealPlan(Scanner scanner) {
        System.out.println("Enter date (YYYY-MM-DD), days from today (number), or weekday (Mon-Sun):");
        String input = scanner.nextLine().trim();
        LocalDate date;

        try {
            if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // Full date format
            date = LocalDate.parse(input);
            } else if (input.matches("\\d+")) {
            // Number of days
            int daysAhead = Integer.parseInt(input);
            date = LocalDate.now().plusDays(daysAhead);
            } else {
            // Weekday name
            String dayName = input.toLowerCase().substring(0, 3);
            date = LocalDate.now();
            switch (dayName) {
                case "mon", "monday" -> { while (date.getDayOfWeek().getValue() != 1) date = date.plusDays(1); }
                case "tue", "tuesday" -> { while (date.getDayOfWeek().getValue() != 2) date = date.plusDays(1); }
                case "wed", "wednesday" -> { while (date.getDayOfWeek().getValue() != 3) date = date.plusDays(1); }
                case "thu", "thursday" -> { while (date.getDayOfWeek().getValue() != 4) date = date.plusDays(1); }
                case "fri", "friday" -> { while (date.getDayOfWeek().getValue() != 5) date = date.plusDays(1); }
                case "sat", "saturday" -> { while (date.getDayOfWeek().getValue() != 6) date = date.plusDays(1); }
                case "sun", "sunday" -> { while (date.getDayOfWeek().getValue() != 7) date = date.plusDays(1); }
                default -> throw new IllegalArgumentException("Invalid weekday");
            }
            }
        } catch (Exception e) {
            System.out.println("Invalid input format.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            System.out.println("Cannot create meal plan for past dates.");
            return;
        }
        
        DailyMeal dailyMeal = new DailyMeal();
        // Print feedback messages when recipes are not found
        
        System.out.println("Enter breakfast recipe name:");
        String breakfastName = scanner.nextLine();
        Recipe breakfastRecipe = recipeBook.stream()
                .filter(recipe -> recipe.getName().equalsIgnoreCase(breakfastName))
                .findFirst()
                .orElse(null);
        if (breakfastRecipe == null) {
            System.out.println("Recipe not found.");
        }
        dailyMeal.setBreakfast(breakfastRecipe);

        System.out.println("Enter lunch recipe name:");
        String lunchName = scanner.nextLine();
        Recipe lunchRecipe = recipeBook.stream()
                .filter(recipe -> recipe.getName().equalsIgnoreCase(lunchName))
                .findFirst()
                .orElse(null);
        if (lunchRecipe == null) {
            System.out.println("Recipe not found.");
        }  
        dailyMeal.setLunch(lunchRecipe);

        System.out.println("Enter dinner recipe name:");
        String dinnerName = scanner.nextLine();
        Recipe dinnerRecipe = recipeBook.stream()
                .filter(recipe -> recipe.getName().equalsIgnoreCase(dinnerName))
                .findFirst()
                .orElse(null);
        if (dinnerRecipe == null) {
            System.out.println("Recipe not found.");
        }
        dailyMeal.setDinner(dinnerRecipe);

        dailyMealPlans.put(date, dailyMeal);
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

    public void printMealPlans() {
        LocalDate today = LocalDate.now();
        boolean hasPlans = false;
        
        System.out.println("=== Meal Plans from " + today.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " onwards ===");
        for (Map.Entry<LocalDate, DailyMeal> entry : dailyMealPlans.entrySet()) {
            if (entry.getKey().isEqual(today) || entry.getKey().isAfter(today)) {
                System.out.println("Date: " + entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " (" + entry.getKey().getDayOfWeek() + ")");
                entry.getValue().printDetails();
                System.out.println("-------------------------");
                hasPlans = true;
            }
        }
        
        if (!hasPlans) {
            System.out.println("No meal plans found for upcoming dates.");
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
    //save pantry and recipe book to json files
    private void savePantry() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("pantry.json"), pantry);
            System.out.println("Pantry wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Pantry: " + e.getMessage());
        }
    }

    private void saveRecipeBook() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("recipebook.json"), recipeBook);
            System.out.println("Rezeptbuch wurde gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern des Rezeptbuchs: " + e.getMessage());
        }
    }

    private void saveMealPlans() {
        // insert jackson wrapper method
        System.out.println("meal plans saved.");
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
