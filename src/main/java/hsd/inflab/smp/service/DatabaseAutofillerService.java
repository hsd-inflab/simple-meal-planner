package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
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
            if ("flyway_schema_history".equalsIgnoreCase(table) || "databasechangelog".equalsIgnoreCase(table)) {
                continue;
            }

            String checkDataSql = "SELECT 1 FROM \"" + table + "\" LIMIT 1";
            try {
                List<Integer> result = jdbcTemplate.query(checkDataSql, (rs, rowNum) -> rs.getInt(1));
                if (!result.isEmpty()) {
                    return false;
                }
            } catch (DataAccessException e) {
                System.err.println("Konnte Tabelle " + table + " nicht prüfen: " + e.getMessage());
            }
        }
        return true;
    }

    @Transactional // Transactional: Steuert Datenbanktransaktionen automatisch nach dem Prinzip „Alles oder nichts“.
    protected void fillDatabase() {
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

        PantryItem flour = new PantryItem(
                "Weizenmehl",
                Unit.KG,
                2.5,
                Category.NONE,
                LocalDate.now().plusMonths(6),
                LocalDate.now(),
                "Diamant",
                1.99);

        PantryItem eggs = new PantryItem(
                "Eier",
                Unit.UNIT,
                30.0,
                Category.NONE,
                LocalDate.now().plusMonths(12),
                LocalDate.now(),
                "Fuerstenhof",
                7.49);

        pantryItemRepository.saveAll(List.of(pasta, tomatoSauce, flour, eggs));
        System.out.println("-> Erstelle Rezepte (Recipe Book)...");

        // -------------------------------------------------------------------------------------

        RecipeIngredient beef =
                new RecipeIngredient("Rinderhackfleisch", Unit.G, 500.0, Category.MEAT, "Fleisch", "Anbraten");
        RecipeIngredient onions =
                new RecipeIngredient("Zwiebeln", Unit.UNIT, 2.0, Category.VEGETABLE, "Gemüse", "Würfeln und dünsten");
        RecipeIngredient milk = new RecipeIngredient(
                "Milch", Unit.ML, 250.0, Category.DAIRY, "Milchprodukt", "Erwärmen und aufschäumen");
        RecipeIngredient espresso =
                new RecipeIngredient("Espresso", Unit.ML, 50.0, Category.NONE, "Kaffee", "Frisch aufbrühen");
        RecipeIngredient puffPastry =
                new RecipeIngredient("Blätterteig", Unit.G, 275.0, Category.NONE, "Teigware", "Ausrollen");
        RecipeIngredient apples = new RecipeIngredient(
                "Äpfel", Unit.UNIT, 4.0, Category.FRUIT, "Kernobst", "Schälen und in Spalten schneiden");

        Recipe bologneseRecipe = new Recipe(
                "Grundbasis Bolognese",
                "Herzhafte Fleischsauce als Basis für Pasta oder Lasagne.",
                List.of(beef, onions));

        Recipe latteRecipe = new Recipe(
                "Caffè Latte",
                "Italienisches Kaffeegetränk mit viel heißer Milch und Milchschaum.",
                List.of(milk, espresso));

        Recipe appleTartRecipe = new Recipe(
                "Schnelle Apfeltarte",
                "Knuspriger Blätterteig belegt mit fruchtigen Apfelspalten.",
                List.of(puffPastry, apples));

        bologneseRecipe = recipeRepository.save(bologneseRecipe);
        latteRecipe = recipeRepository.save(latteRecipe);
        appleTartRecipe = recipeRepository.save(appleTartRecipe);

        // --------------------------------------------------------------------------------------

        System.out.println("-> Verknüpfe Rezepte mit dem Kalender (Daily Meals)...");

        DailyMeal todayPlan = new DailyMeal();
        todayPlan.setMealDate(LocalDate.now());

        // Verknüpfung mit den oben gespeicherten Rezepten
        todayPlan.setBreakfastRecipe(bologneseRecipe);
        todayPlan.setBreakfastServings(1);

        todayPlan.setLunchRecipe(latteRecipe);
        todayPlan.setLunchServings(2);

        DailyMeal nextDayPlan = new DailyMeal();
        nextDayPlan.setMealDate(LocalDate.now().plusDays(1));

        nextDayPlan.setBreakfastRecipe(bologneseRecipe);
        nextDayPlan.setBreakfastServings(2);

        nextDayPlan.setLunchRecipe(latteRecipe);
        nextDayPlan.setLunchServings(1);

        DailyMeal nextTwoDaysPlan = new DailyMeal();
        nextTwoDaysPlan.setMealDate(LocalDate.now().plusDays(2));

        nextTwoDaysPlan.setDinnerRecipe(appleTartRecipe);
        nextTwoDaysPlan.setDinnerServings(2);

        dailyMealRepository.save(todayPlan);
        dailyMealRepository.save(nextDayPlan);
        dailyMealRepository.save(nextTwoDaysPlan);

        System.out.println("✓ Datenbank-Autofill erfolgreich abgeschlossen!");
    }
}
