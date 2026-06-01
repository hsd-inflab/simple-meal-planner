package hsd.inflab.smp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
    void run_shouldFillDatabase_whenNoTablesExist() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of());

        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldSkipAutofill_whenAnyTableContainsData() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of("pantry_item"));

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of(1));

        // when
        service.run();

        // then
        verify(service, never()).fillDatabase();
    }

    @Test
    void run_shouldFillDatabase_whenTablesExistButAreEmpty() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("pantry_item", "recipe", "daily_meal"));

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of());

        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldIgnoreFlywaySchemaHistoryTable() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of("flyway_schema_history"));

        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(jdbcTemplate, never())
                .query(contains("flyway_schema_history"), any(org.springframework.jdbc.core.RowMapper.class));

        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldIgnoreDatabaseChangelogTable() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of("databasechangelog"));

        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(jdbcTemplate, never())
                .query(contains("databasechangelog"), any(org.springframework.jdbc.core.RowMapper.class));

        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldContinueChecking_whenCheckingOneTableThrowsDataAccessException() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("broken_table", "empty_table"));

        when(jdbcTemplate.query(contains("\"broken_table\""), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenThrow(new DataAccessResourceFailureException("Table check failed"));

        when(jdbcTemplate.query(contains("\"empty_table\""), ArgumentMatchers.<RowMapper<Integer>>any()))
                .thenReturn(List.of());

        doNothing().when(service).fillDatabase();

        // when
        service.run();

        // then
        verify(service, times(1)).fillDatabase();
    }

    @Test
    void run_shouldSkipAutofill_whenSecondTableContainsData() throws Exception {
        // given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("empty_table", "filled_table"));

        when(jdbcTemplate.query(contains("\"empty_table\""), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of());

        when(jdbcTemplate.query(contains("\"filled_table\""), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of(1));

        // when
        service.run();

        // then
        verify(service, never()).fillDatabase();
    }
}
