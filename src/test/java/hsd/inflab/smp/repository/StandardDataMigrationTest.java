package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;

/**
 * Verifies how the legacy rows created before multi-tenancy are normalized: recipes without an owner become the
 * shared standard recipes, everything personal without an owner is removed because it can no longer be reached.
 */
class StandardDataMigrationTest {

    private static final String TENANT_MIGRATION = "db/migration/V3__add_tenant_ownership.sql";
    private static final String STANDARD_DATA_MIGRATION = "db/migration/V4__normalize_legacy_tenant_data.sql";

    @Test
    void migration_marksOwnerlessRecipesAsGlobal() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithTenantSchema()) {
            UUID legacyRecipeId = UUID.randomUUID();
            insertRecipe(connection, legacyRecipeId, null, false);

            // Act
            runMigration(connection, STANDARD_DATA_MIGRATION);

            // Assert
            assertThat(isGlobal(connection, "recipe_book", legacyRecipeId)).isTrue();
        }
    }

    @Test
    void migration_keepsUserOwnedRecipesPrivate() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithTenantSchema()) {
            UUID ownerId = insertUser(connection, "recipe-owner");
            UUID ownedRecipeId = UUID.randomUUID();
            insertRecipe(connection, ownedRecipeId, ownerId, false);

            // Act
            runMigration(connection, STANDARD_DATA_MIGRATION);

            // Assert
            assertThat(isGlobal(connection, "recipe_book", ownedRecipeId)).isFalse();
        }
    }

    @Test
    void migration_removesOwnerlessPantryItems() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithTenantSchema()) {
            UUID ownerId = insertUser(connection, "pantry-owner");
            UUID orphanedItemId = UUID.randomUUID();
            UUID ownedItemId = UUID.randomUUID();
            insertPantryItem(connection, orphanedItemId, null);
            insertPantryItem(connection, ownedItemId, ownerId);

            // Act
            runMigration(connection, STANDARD_DATA_MIGRATION);

            // Assert
            assertThat(rowExists(connection, "pantry", orphanedItemId)).isFalse();
            assertThat(rowExists(connection, "pantry", ownedItemId)).isTrue();
        }
    }

    @Test
    void migration_removesOwnerlessDailyMeals() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithTenantSchema()) {
            UUID ownerId = insertUser(connection, "meal-plan-owner");
            UUID orphanedMealId = UUID.randomUUID();
            UUID ownedMealId = UUID.randomUUID();
            insertDailyMeal(connection, orphanedMealId, LocalDate.of(2026, 7, 11), null);
            insertDailyMeal(connection, ownedMealId, LocalDate.of(2026, 7, 12), ownerId);

            // Act
            runMigration(connection, STANDARD_DATA_MIGRATION);

            // Assert
            assertThat(rowExists(connection, "daily_meal", orphanedMealId)).isFalse();
            assertThat(rowExists(connection, "daily_meal", ownedMealId)).isTrue();
        }
    }

    private Connection openDatabaseWithTenantSchema() throws SQLException {
        String databaseName =
                "standard_data_migration_" + UUID.randomUUID().toString().replace("-", "");
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE app_user (id UUID PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL)");
            statement.execute(
                    "CREATE TABLE recipe_book (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT)");
            statement.execute(
                    "CREATE TABLE pantry (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, price DOUBLE PRECISION NOT NULL DEFAULT 0)");
            statement.execute(
                    "CREATE TABLE daily_meal (id UUID PRIMARY KEY, meal_date DATE NOT NULL, CONSTRAINT daily_meal_meal_date_key UNIQUE (meal_date))");
        }
        runMigration(connection, TENANT_MIGRATION);
        return connection;
    }

    private void runMigration(Connection connection, String resource) throws SQLException {
        InputStream migration = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        assertThat(migration).as("migration %s", resource).isNotNull();
        RunScript.execute(connection, new InputStreamReader(migration, StandardCharsets.UTF_8));
    }

    private UUID insertUser(Connection connection, String username) throws SQLException {
        UUID userId = UUID.randomUUID();
        try (var statement =
                connection.prepareStatement("INSERT INTO app_user (id, username, password) VALUES (?, ?, ?)")) {
            statement.setObject(1, userId);
            statement.setString(2, username);
            statement.setString(3, "test-password-hash");
            statement.executeUpdate();
        }
        return userId;
    }

    private void insertRecipe(Connection connection, UUID id, UUID userId, boolean global) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO recipe_book (id, name, user_id, is_global) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setString(2, "Legacy recipe");
            statement.setObject(3, userId);
            statement.setBoolean(4, global);
            statement.executeUpdate();
        }
    }

    private void insertPantryItem(Connection connection, UUID id, UUID userId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO pantry (id, name, price, user_id, is_global) VALUES (?, ?, ?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setString(2, "Legacy item");
            statement.setDouble(3, 1.99);
            statement.setObject(4, userId);
            statement.setBoolean(5, false);
            statement.executeUpdate();
        }
    }

    private void insertDailyMeal(Connection connection, UUID id, LocalDate date, UUID userId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO daily_meal (id, meal_date, user_id, is_global) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setObject(2, date);
            statement.setObject(3, userId);
            statement.setBoolean(4, false);
            statement.executeUpdate();
        }
    }

    private boolean isGlobal(Connection connection, String table, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT is_global FROM " + table + " WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                assertThat(result.next()).as("row %s in %s", id, table).isTrue();
                return result.getBoolean(1);
            }
        }
    }

    private boolean rowExists(Connection connection, String table, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
