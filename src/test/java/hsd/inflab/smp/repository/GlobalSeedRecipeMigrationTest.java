package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;

class GlobalSeedRecipeMigrationTest {

    private static final String MIGRATION_RESOURCE = "db/migration/V4__mark_seed_recipes_global.sql";

    @Test
    void migration_marksOnlyKnownSeedRecipesAsGlobal() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithTenantSchema()) {
            insertRecipe(connection, "Grundbasis Bolognese");
            insertRecipe(connection, "Caffè Latte");
            insertRecipe(connection, "Schnelle Apfeltarte");
            insertRecipe(connection, "Orphaned user recipe");

            // Act
            runMigration(connection);

            // Assert
            assertThat(isGlobal(connection, "Grundbasis Bolognese")).isTrue();
            assertThat(isGlobal(connection, "Caffè Latte")).isTrue();
            assertThat(isGlobal(connection, "Schnelle Apfeltarte")).isTrue();
            assertThat(isGlobal(connection, "Orphaned user recipe")).isFalse();
        }
    }

    private Connection openDatabaseWithTenantSchema() throws SQLException {
        String databaseName =
                "global_seed_recipe_migration_" + UUID.randomUUID().toString().replace("-", "");
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE recipe_book (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT, user_id UUID, is_global BOOLEAN NOT NULL DEFAULT FALSE)");
        }
        return connection;
    }

    private void insertRecipe(Connection connection, String name) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO recipe_book (id, name, user_id, is_global) VALUES (?, ?, NULL, FALSE)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    private void runMigration(Connection connection) throws SQLException {
        InputStream migration = Thread.currentThread().getContextClassLoader().getResourceAsStream(MIGRATION_RESOURCE);
        assertThat(migration)
                .as("global seed recipe migration %s", MIGRATION_RESOURCE)
                .isNotNull();
        RunScript.execute(connection, new InputStreamReader(migration, StandardCharsets.UTF_8));
    }

    private boolean isGlobal(Connection connection, String name) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT is_global FROM recipe_book WHERE name = ?")) {
            statement.setString(1, name);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getBoolean("is_global");
            }
        }
    }
}
