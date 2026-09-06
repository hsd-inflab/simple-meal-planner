package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Fail-closed guard: the autofiller only runs where it is explicitly switched on. It seeds the shared standard
// recipes that user registration depends on, so every environment decides this through configuration alone.
@Component
@ConditionalOnProperty(prefix = "app.autofill", name = "enabled", havingValue = "true")
public class DatabaseAutofillerService implements CommandLineRunner {

    // Only the global standard recipes decide whether the autofill runs. User owned data (pantry, daily_meal) is
    // created during registration and must never block the seeding of the shared standard data.
    private static final String GLOBAL_RECIPE_CHECK_SQL =
            "SELECT 1 FROM \"recipe_book\" WHERE is_global = TRUE LIMIT 1";

    private final JdbcTemplate jdbcTemplate;
    private final RecipeRepository recipeRepository;

    public DatabaseAutofillerService(JdbcTemplate jdbcTemplate, RecipeRepository recipeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (globalRecipesMissing()) {
            System.out.println("Keine globalen Standardrezepte vorhanden. Starte Autofill...");
            fillDatabase();
        } else {
            System.out.println("Globale Standardrezepte sind bereits vorhanden. Autofill wird übersprungen.");
        }
    }

    // Returns true when no global standard recipe exists yet. A failing check is treated as "missing" so that a
    // freshly migrated environment still gets its standard data.
    private boolean globalRecipesMissing() {
        try {
            return jdbcTemplate
                    .query(GLOBAL_RECIPE_CHECK_SQL, (rs, rowNum) -> rs.getInt(1))
                    .isEmpty();
        } catch (DataAccessException e) {
            System.err.println("Konnte globale Standardrezepte nicht prüfen: " + e.getMessage());
            return true;
        }
    }

    @Transactional // Transactional: Steuert Datenbanktransaktionen automatisch nach dem Prinzip „Alles oder nichts“.
    protected void fillDatabase() {
        System.out.println("-> Erstelle globale Standardrezepte (Recipe Book)...");

        RecipeIngredient beef =
                new RecipeIngredient("Rinderhackfleisch", Unit.G, 500.0, Category.MEAT, "Fleisch", "Anbraten");
        RecipeIngredient onions =
                new RecipeIngredient("Zwiebeln", Unit.UNIT, 2.0, Category.VEGETABLE, "Gemüse", "Würfeln und dünsten");
        RecipeIngredient milk = new RecipeIngredient(
                "Milch", Unit.ML, 250.0, Category.DAIRY, "Milchprodukt", "Erwärmen und aufschäumen");
        RecipeIngredient espresso =
                new RecipeIngredient("Espresso", Unit.ML, 50.0, Category.BEVERAGE, "Kaffee", "Frisch aufbrühen");
        RecipeIngredient puffPastry =
                new RecipeIngredient("Blätterteig", Unit.G, 275.0, Category.STARCH, "Teigware", "Ausrollen");
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

        recipeRepository.saveAll(List.of(asGlobal(bologneseRecipe), asGlobal(latteRecipe), asGlobal(appleTartRecipe)));

        System.out.println("✓ Datenbank-Autofill erfolgreich abgeschlossen!");
    }

    // Standard recipes belong to no single user: they are visible to everyone and therefore carry no owner.
    private Recipe asGlobal(Recipe recipe) {
        recipe.setGlobal(true);
        return recipe;
    }
}
