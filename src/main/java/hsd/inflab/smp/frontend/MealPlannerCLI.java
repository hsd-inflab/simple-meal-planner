package hsd.inflab.smp.frontend;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.service.MealPlannerService;

import java.time.LocalDate;
import java.util.*;

@Deprecated
public class MealPlannerCLI {
    private MealPlannerService mealPlanner;

    public MealPlannerCLI(MealPlannerService mealPlanner) {
        this.mealPlanner = mealPlanner;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US); // enables entering doubles with . instead of ,
        System.out.println("Welcome to the HSD:MealPlanner.");

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show recipes");
            System.out.println("11. add recipes");
            System.out.println("2. Show pantry");
            System.out.println("21. add groceries");
            System.out.println("22. change expiration date of a specific pantry item (TEST-FUNCTION)");
            System.out.println("3. Show possible recipes");
            System.out.println("4. Show meal plans");
            System.out.println("41. add meal plans");
            System.out.println("0. Exit");
            System.out.print("Input menu point: ");

            try {
                int input = Integer.parseInt(scanner.nextLine());
                switch (input) {
                    case 1 -> printAllRecipes(mealPlanner.getRecipeBook());
                    case 11 -> addRecipe(scanner, mealPlanner.getRecipeBook());
                    case 2 -> printPantryContents(mealPlanner.getPantry());
                    case 21 -> addGroceries(scanner, mealPlanner.getPantry());
                    case 22 -> changePantryItemExpirationDate(scanner, mealPlanner.getPantry());
                    case 3 -> printAvailableRecipes(
                            getAvailableRecipes(mealPlanner.getRecipeBook(), mealPlanner.getPantry()));
                    case 4 -> printMealPlans(mealPlanner.getDailyMealPlans());
                    case 41 -> addMealPlan(scanner, mealPlanner.getRecipeBook(), mealPlanner.getDailyMealPlans());
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

        // scanner.close();
    }

    public void printAllRecipes(List<Recipe> recipeBook) {
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

    private void addRecipe(Scanner scanner, List<Recipe> recipeBook) {
        String recipeName, ingredient, description, unit, category, foodType, preparation;
        double amountPerPerson;

        recipeName = readString(scanner, "Enter The name of the recipe:");
        description = readString(scanner, "Enter The description on how to cook this recipe:");

        Recipe recipe = new Recipe(recipeName, description, new ArrayList<>());

        while (true) {
            String input = readString(scanner, "Enter ingredient or type exit to save recipe:");

            if (input.equals("exit")) {
                break;
            }

            ingredient = input;
            unit = readString(scanner, "Enter the unit for this ingredient:");
            amountPerPerson = readDouble(scanner, "Enter the amount per person:");
            category = readString(scanner, "Enter the food category (meat, vegetable, dairy, etc.):");
            foodType = readString(scanner, "Enter the food type:");
            preparation = readString(scanner, "Enter the needed preparation of ingredient (sliced, scrambled, etc.) :");

            // RecipeIngredient recipeIngredient = new RecipeIngredient(ingredient, unit, amountPerPerson, category,
            // foodType, preparation);
            // recipe.addIngredient(recipeIngredient);
        }

        recipeBook.add(recipe);
    }

    public void printPantryContents(List<PantryItem> pantry) {
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

    private void addGroceries(Scanner scanner, List<PantryItem> pantry) {
        String name, unit, category, brand, purchaseDateString, expirationDateString;
        double amount, price;
        LocalDate purchaseDate;
        int daysTillExpiration;

        name = readString(scanner, "Enter the name of the Grocery:");
        unit = readString(scanner, "unit of measurement (unit, g, kg, teaspoon, tablespoon, L, mL):");
        amount = readDouble(scanner, "amount:");
        category = readString(scanner, "food category (meat, vegetable, fruit):");
        daysTillExpiration = readInt(scanner, "days until expiration:");
        brand = readString(scanner, "Product brand:");
        price = readDouble(scanner, "price:");

        purchaseDate = LocalDate.now();
        LocalDate expirationDate = purchaseDate.plusDays(daysTillExpiration);

        // PantryItem item = new PantryItem(name, unit, amount, category, expirationDate, purchaseDate, brand, price);
        // pantry.add(item);
    }

    // Eine Methode, um zu Testen, ob das Ablaufdatum eines Pantry-Items geändert werden kann.
    // Gleichzeitig wird geprüft, ob in der Json Datei das Datum mit dem richtigen Typen gespeichert wird.
    public void changePantryItemExpirationDate(Scanner scanner, List<PantryItem> pantry) {
        String name = readString(scanner, "Name des Pantry-Items, dessen Ablaufdatum geändert werden soll:");
        PantryItem item = pantry.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (item == null) {
            System.out.println("Kein Pantry-Item mit diesem Namen gefunden.");
            return;
        }

        LocalDate newDateStr = item.getExpirationDate();
        try {
            // Validierung des Datums
            int days = 100; // Beispiel: Anzahl der Tage, um die das Ablaufdatum geändert werden soll
            LocalDate newLocalDate = newDateStr.plusDays(days); // Beispiel: Ablaufdatum um 100 Tage verlängern

            item.setExpirationDate(newLocalDate);
            System.out.println("Ablaufdatum erfolgreich um " + days + " Tage geändert.");
        } catch (Exception e) {
            System.out.println("Ungültiges Datumsformat.");
        }
    }

    public List<Recipe> getAvailableRecipes(List<Recipe> recipeBook, List<PantryItem> pantry) {
        List<Recipe> availableRecipes = new ArrayList<>();

        for (Recipe recipe : recipeBook) {
            boolean canMake = true;

            // Durchlaufe alle Zutaten des Rezepts
            for (RecipeIngredient recipeIng : recipe.getIngredients()) {
                // Suche nach der Zutat in der Pantry
                Optional<PantryItem> matchingItem = pantry.stream()
                        .filter(p ->
                                p.getName().equalsIgnoreCase(recipeIng.getName())) // Vergleiche die Namen der Zutaten
                        .findFirst(); // Finde das erste PantryItem, das der Zutat entspricht

                // Prüfe, ob die Zutat in der Pantry vorhanden ist und ob die Menge ausreicht
                if (matchingItem.isEmpty() || matchingItem.get().getAmount() < recipeIng.getAmount()) {
                    canMake = false; // Rezept kann nicht gemacht werden, da Zutat fehlt oder Menge nicht ausreicht
                    break; // Schleife abbrechen, da es nicht mehr möglich ist, das Rezept zu machen
                }
            }

            // Wenn das Rezept mit den Zutaten zubereitet werden kann, füge es zur Liste der verfügbaren Rezepte hinzu
            if (canMake) {
                availableRecipes.add(recipe);
            }
        }

        return availableRecipes;
    }

    public void printAvailableRecipes(List<Recipe> possibleRecipes) {
        if (possibleRecipes.isEmpty()) {
            System.out.println("No recipes can be made with the current pantry items.");
        } else {
            System.out.println("You can make the following recipes:");
            possibleRecipes.forEach(recipe -> System.out.println(recipe.getName()));
        }
    }

    public void printMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {
        LocalDate today = LocalDate.now();
        boolean hasPlans = false;

        System.out.println("=== Meal Plans from "
                + today.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " onwards ===");
        for (Map.Entry<LocalDate, DailyMeal> entry : dailyMealPlans.entrySet()) {
            if (entry.getKey().isEqual(today) || entry.getKey().isAfter(today)) {
                System.out.println(
                        "Date: " + entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                + " (" + entry.getKey().getDayOfWeek() + ")");
                entry.getValue().printDetails();
                System.out.println("-------------------------");
                hasPlans = true;
            }
        }

        if (!hasPlans) {
            System.out.println("No meal plans found for upcoming dates.");
        }
    }

    private void addMealPlan(Scanner scanner, List<Recipe> recipeBook, Map<LocalDate, DailyMeal> dailyMealPlans) {
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
                    case "mon", "monday" -> {
                        while (date.getDayOfWeek().getValue() != 1) {
                            date = date.plusDays(1);
                        }
                    }
                    case "tue", "tuesday" -> {
                        while (date.getDayOfWeek().getValue() != 2) {
                            date = date.plusDays(1);
                        }
                    }
                    case "wed", "wednesday" -> {
                        while (date.getDayOfWeek().getValue() != 3) {
                            date = date.plusDays(1);
                        }
                    }
                    case "thu", "thursday" -> {
                        while (date.getDayOfWeek().getValue() != 4) {
                            date = date.plusDays(1);
                        }
                    }
                    case "fri", "friday" -> {
                        while (date.getDayOfWeek().getValue() != 5) {
                            date = date.plusDays(1);
                        }
                    }
                    case "sat", "saturday" -> {
                        while (date.getDayOfWeek().getValue() != 6) {
                            date = date.plusDays(1);
                        }
                    }
                    case "sun", "sunday" -> {
                        while (date.getDayOfWeek().getValue() != 7) {
                            date = date.plusDays(1);
                        }
                    }
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

        String dinnerName = readString(scanner, "Enter dinner recipe name:");
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

    // Helperfunctions
    private double readDouble(Scanner scanner, String text) {
        double input;
        // boolean success = false;
        while (true) {
            System.out.println(text);
            try {
                input = scanner.nextDouble();
                return input;
            } catch (Exception e) {
                System.out.println("That was not a number. Try again.");
            } finally {
                scanner.nextLine();
            }
        }
    }

    private int readInt(Scanner scanner, String text) {
        int input;
        // boolean success = false;
        while (true) {
            System.out.println(text);
            try {
                input = scanner.nextInt();
                return input;
            } catch (Exception e) {
                System.out.println("That was not an Integer. Try again.");
            } finally {
                scanner.nextLine();
            }
        }
    }

    private String readString(Scanner scanner, String text) {
        String input;
        // boolean success = false;
        while (true) {
            System.out.println(text);
            try {
                input = scanner.nextLine();
                return input;
            } catch (Exception e) {
                System.out.println("That was not a String. Try again.");
            }
        }
    }
}
