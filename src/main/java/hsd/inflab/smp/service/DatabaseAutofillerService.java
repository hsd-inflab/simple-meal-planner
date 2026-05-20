package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.*;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseAutofillerService implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DailyMealRepository dailyMealRepository;
    private final PantryItemRepository pantryItemRepository;
    private final RecipeRepository recipeRepository;

    public DatabaseAutofillerService(
            JdbcTemplate jdbcTemplate,
            DailyMealRepository dailyMealRepository,
            PantryItemRepository pantryItemRepository,
            RecipeRepository recipeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.dailyMealRepository = dailyMealRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (isDatabaseEmpty()) {
            System.out.println("PostgreSQL-Datenbank ist komplett leer. Starte Autofill...");
            fillDatabase();
        } else {
            System.out.println("Datenbank enthält bereits Daten. Autofill wird übersprungen.");
        }
    }

    private boolean isDatabaseEmpty() {
        String findTablesSql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        List<String> tables = jdbcTemplate.queryForList(findTablesSql, String.class);

        if (tables.isEmpty()) {
            return true;
        }

        for (String table : tables) {
            if (table.equalsIgnoreCase("flyway_schema_history") || table.equalsIgnoreCase("databasechangelog")) {
                continue;
            }

            String checkDataSql = "SELECT 1 FROM \"" + table + "\" LIMIT 1";
            try {
                List<Integer> result = jdbcTemplate.query(checkDataSql, (rs, rowNum) -> rs.getInt(1));
                if (!result.isEmpty()) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Konnte Tabelle " + table + " nicht prüfen: " + e.getMessage());
            }
        }
        return true;
    }

    @Transactional
    protected void fillDatabase() {
        // ==========================================
        // 1. PANTRY ITEMS (VORRAT) SPEICHERN
        // ==========================================
        System.out.println("-> Erstelle Vorratsdaten (Pantry)...");

        PantryItem pasta = new PantryItem(
                "Spaghetti",
                Unit.G,
                500.0,
                Category.NONE, // Angenommene Enums
                LocalDate.now().plusYears(1),
                LocalDate.now(),
                "Barilla",
                1.99);

        PantryItem tomatoSauce = new PantryItem(
                "Tomatensauce",
                Unit.UNIT,
                2.0,
                Category.VEGETABLE,
                LocalDate.now().plusMonths(6),
                LocalDate.now(),
                "Oro di Parma",
                2.49);

        pantryItemRepository.saveAll(List.of(pasta, tomatoSauce));

        // ==========================================
        // 2. REZEPTE & REZEPTZUTATEN SPEICHERN
        // ==========================================
        System.out.println("-> Erstelle Rezepte (Recipe Book)...");

        // Zutaten für Rezept 1 (Spaghetti Pomodoro)
        RecipeIngredient spaghettiZutat =
                new RecipeIngredient("Spaghetti", Unit.G, 125.0, Category.NONE, "Nudel", "In Salzwasser kochen");
        RecipeIngredient sauceZutat =
                new RecipeIngredient("Tomatensauce", Unit.ML, 200.0, Category.VEGETABLE, "Sauce", "Erwärmen");

        // Rezept 1 erstellen (Nutzt deinen Custom-Konstruktor)
        Recipe spaghettiRecipe = new Recipe(
                "Spaghetti Pomodoro",
                "Klassische italienische Pasta mit fruchtiger Tomatensauce.",
                List.of(spaghettiZutat, sauceZutat));

        // Zutaten für Rezept 2 (Oatmeal / Frühstück)
        RecipeIngredient oats =
                new RecipeIngredient("Haferflocken", Unit.G, 50.0, Category.NONE, "Getreide", "Mit Milch aufkochen");
        Recipe porridgeRecipe =
                new Recipe("Porridge", "Warmes, nahrhaftes Frühstück für einen guten Start in den Tag.", List.of(oats));

        // Rezepte speichern (Speichert dank CascadeType.ALL die Zutaten automatisch mit!)
        spaghettiRecipe = recipeRepository.save(spaghettiRecipe);
        porridgeRecipe = recipeRepository.save(porridgeRecipe);

        // ==========================================
        // 3. DAILY MEALS (TAGESPLANER) SPEICHERN
        // ==========================================
        System.out.println("-> Verknüpfe Rezepte mit dem Kalender (Daily Meals)...");

        // Da DailyMeal ein einzigartiges Datum fordert (unique = true), nutzen wir das heutige Datum
        DailyMeal todayPlan = new DailyMeal();
        todayPlan.setMealDate(LocalDate.now());

        // Verknüpfung mit den oben gespeicherten Rezepten
        todayPlan.setBreakfastRecipe(porridgeRecipe);
        todayPlan.setBreakfastServings(1);

        todayPlan.setLunchRecipe(spaghettiRecipe);
        todayPlan.setLunchServings(2); // z.B. für 2 Personen kochen

        // Dinner lassen wir im Beispiel einfach mal leer (null), da es laut Modell optional zu sein scheint

        dailyMealRepository.save(todayPlan);

        System.out.println("✓ Datenbank-Autofill erfolgreich abgeschlossen!");
    }
}
