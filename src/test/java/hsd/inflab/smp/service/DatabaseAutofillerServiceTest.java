package hsd.inflab.smp.service;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class DatabaseAutofillerServiceTest {

    private JdbcTemplate jdbcTemplate;
    private DailyMealRepository dailyMealRepository;
    private PantryItemRepository pantryItemRepository;
    private RecipeRepository recipeRepository;

    private DatabaseAutofillerService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        dailyMealRepository = mock(DailyMealRepository.class);
        pantryItemRepository = mock(PantryItemRepository.class);
        recipeRepository = mock(RecipeRepository.class);

        service = Mockito.spy(new DatabaseAutofillerService(
                jdbcTemplate, dailyMealRepository, pantryItemRepository, recipeRepository));
    }

    @Test
    void run_shouldFillDatabase_whenAllTargetTablesAreEmpty() throws Exception {
        // given: jede Tabellenprüfung liefert eine leere Liste
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldSkipAutofill_whenAnyTargetTableContainsData() throws Exception {
        // given: pantry enthält Daten, der Rest ist leer
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        when(jdbcTemplate.query(contains("\"pantry\""), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of(1));

        // when
        service.run();

        // then
        verify(service, never()).fillDatabase();
    }

    @Test
    void run_shouldContinueChecking_whenCheckingOneTableThrowsDataAccessException() throws Exception {
        // given: eine Tabelle wirft, die übrigen sind leer -> Autofill soll trotzdem laufen
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        when(jdbcTemplate.query(contains("\"daily_meal\""), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenThrow(new DataAccessResourceFailureException("Table check failed"));
        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldOnlyCheckTargetTables_andIgnoreAuthAndFlywayTables() throws Exception {
        // given: alle Fachtabellen leer
        when(jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());
        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then: nur die vier Fachtabellen werden geprüft
        verify(jdbcTemplate).query(contains("\"daily_meal\""), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate).query(contains("\"pantry\""), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate).query(contains("\"recipe_book\""), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate).query(contains("\"recipe_ingredients\""), ArgumentMatchers.<RowMapper<Integer>>any());

        // Auth- und Flyway-Tabellen dürfen nicht abgefragt werden
        verify(jdbcTemplate, never()).query(contains("app_user"), ArgumentMatchers.<RowMapper<Integer>>any());
        verify(jdbcTemplate, never())
                .query(contains("flyway_schema_history"), ArgumentMatchers.<RowMapper<Integer>>any());

        // und es findet keine information_schema-Abfrage mehr statt
        verify(jdbcTemplate, never()).queryForList(ArgumentMatchers.anyString(), ArgumentMatchers.eq(String.class));
        verify(service, times(1)).fillDatabase();
    }
}
