package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;

class TenantMigrationTest {

    private static final String MIGRATION_RESOURCE = "db/migration/V3__add_tenant_ownership.sql";

    @Test
    void migration_addsTenantColumnsAndUserForeignKeys() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithPreTenantSchema()) {
            // Act
            runTenantMigration(connection);

            // Assert
            assertThat(columnNames(connection, "RECIPE_BOOK")).contains("USER_ID", "IS_GLOBAL");
            assertThat(columnNames(connection, "PANTRY")).contains("USER_ID", "IS_GLOBAL");
            assertThat(columnNames(connection, "DAILY_MEAL")).contains("USER_ID", "IS_GLOBAL");

            assertThatThrownBy(() -> insertRecipe(connection, UUID.randomUUID(), UUID.randomUUID(), false))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void migration_allowsDifferentUsersToStoreMealPlansForTheSameDate() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithPreTenantSchema()) {
            runTenantMigration(connection);
            UUID firstUserId = insertUser(connection, "first-user");
            UUID secondUserId = insertUser(connection, "second-user");
            LocalDate date = LocalDate.of(2026, 7, 11);

            // Act and Assert
            assertThatCode(() -> {
                        insertDailyMeal(connection, UUID.randomUUID(), date, firstUserId, false);
                        insertDailyMeal(connection, UUID.randomUUID(), date, secondUserId, false);
                    })
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void migration_rejectsTwoMealPlansForTheSameUserAndDate() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithPreTenantSchema()) {
            runTenantMigration(connection);
            UUID userId = insertUser(connection, "meal-plan-owner");
            LocalDate date = LocalDate.of(2026, 7, 11);
            insertDailyMeal(connection, UUID.randomUUID(), date, userId, false);

            // Act and Assert
            assertThatThrownBy(() -> insertDailyMeal(connection, UUID.randomUUID(), date, userId, false))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void migration_allowsExplicitGlobalRecordsWithoutAnOwner() throws Exception {
        // Arrange
        try (Connection connection = openDatabaseWithPreTenantSchema()) {
            runTenantMigration(connection);

            // Act and Assert
            assertThatCode(() -> insertRecipe(connection, UUID.randomUUID(), null, true))
                    .doesNotThrowAnyException();
        }
    }

    private Connection openDatabaseWithPreTenantSchema() throws SQLException {
        String databaseName = "tenant_migration_" + UUID.randomUUID().toString().replace("-", "");
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
        return connection;
    }

    private void runTenantMigration(Connection connection) throws SQLException {
        InputStream migration = Thread.currentThread().getContextClassLoader().getResourceAsStream(MIGRATION_RESOURCE);
        assertThat(migration).as("tenant migration %s", MIGRATION_RESOURCE).isNotNull();
        RunScript.execute(connection, new InputStreamReader(migration, StandardCharsets.UTF_8));
    }

    private Set<String> columnNames(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet result = connection.getMetaData().getColumns(null, null, tableName, null)) {
            while (result.next()) {
                columns.add(result.getString("COLUMN_NAME"));
            }
        }
        return columns;
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
            statement.setString(2, "Test recipe");
            statement.setObject(3, userId);
            statement.setBoolean(4, global);
            statement.executeUpdate();
        }
    }

    private void insertDailyMeal(Connection connection, UUID id, LocalDate date, UUID userId, boolean global)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO daily_meal (id, meal_date, user_id, is_global) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setObject(2, date);
            statement.setObject(3, userId);
            statement.setBoolean(4, global);
            statement.executeUpdate();
        }
    }
}
