package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * The autofiller seeds global standard recipes only. Everything a single user owns (pantry items, daily meals) is
 * created during registration and is therefore out of scope for this component.
 */
class DatabaseAutofillerServiceTest {

    private JdbcTemplate jdbcTemplate;
    private RecipeRepository recipeRepository;

    private DatabaseAutofillerService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        recipeRepository = mock(RecipeRepository.class);

        service = Mockito.spy(new DatabaseAutofillerService(jdbcTemplate, recipeRepository));
    }

    @Test
    void run_fillsDatabase_whenNoGlobalRecipeExists() throws Exception {
        // Arrange
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        doNothing().when(service).fillDatabase();

        // Act
        service.run();

        // Assert
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_skipsAutofill_whenGlobalRecipeExists() throws Exception {
        // Arrange
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of(1));

        // Act
        service.run();

        // Assert
        verify(service, never()).fillDatabase();
    }

    @Test
    void run_stillFills_whenUserOwnedDataExists() throws Exception {
        // Arrange: only the global recipe check answers; user owned tables must not influence the decision
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        doNothing().when(service).fillDatabase();

        // Act
        service.run();

        // Assert
        verify(service, times(1)).fillDatabase();
        verify(jdbcTemplate, never()).query(contains("\"pantry\""), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate, never()).query(contains("\"daily_meal\""), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate, never())
                .query(contains("\"recipe_ingredients\""), ArgumentMatchers.<RowMapper<Integer>>any());
    }

    @Test
    void run_fillsDatabase_whenCheckThrowsDataAccessException() throws Exception {
        // Arrange
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenThrow(new DataAccessResourceFailureException("Table check failed"));
        doNothing().when(service).fillDatabase();

        // Act
        service.run();

        // Assert
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_checksGlobalRecipesOnly_andIgnoresAuthAndFlywayTables() throws Exception {
        // Arrange
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        doNothing().when(service).fillDatabase();

        // Act
        service.run();

        // Assert: exactly one check, and it is restricted to global recipes
        ArgumentCaptor<String> executedSql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(executedSql.capture(), ArgumentMatchers.<RowMapper<Integer>>any());
        assertThat(executedSql.getValue()).contains("recipe_book").containsIgnoringCase("is_global");

        verify(jdbcTemplate, never()).query(contains("app_user"), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate, never())
                .query(contains("flyway_schema_history"), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate, never()).queryForList(ArgumentMatchers.anyString(), ArgumentMatchers.eq(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fillDatabase_seedsGlobalRecipesWithoutOwner() {
        // Arrange
        ArgumentCaptor<List<Recipe>> savedRecipes = ArgumentCaptor.forClass(List.class);

        // Act
        service.fillDatabase();

        // Assert
        verify(recipeRepository).saveAll(savedRecipes.capture());
        assertThat(savedRecipes.getValue()).isNotEmpty().allSatisfy(recipe -> {
            assertThat(recipe.isGlobal()).isTrue();
            assertThat(recipe.getOwner()).isNull();
        });
    }

    @Test
    void fillDatabase_createsNoUserOwnedData() {
        // Act
        service.fillDatabase();

        // Assert: pantry items and daily meals belong to registration, not to the autofiller
        verify(recipeRepository, times(1)).saveAll(ArgumentMatchers.anyList());
        Mockito.verifyNoMoreInteractions(recipeRepository);
        Mockito.verifyNoInteractions(jdbcTemplate);
    }
}
