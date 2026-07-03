package hsd.inflab.smp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.request.DailyMealRequestDto;
import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.security.JwtAuthenticationFilter;
import hsd.inflab.smp.service.DailyMealService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testklasse für den DailyMealController.
 *
 * Ziel:
 * - Controller isoliert testen
 * - HTTP-Statuscodes prüfen
 * - JSON-Antworten prüfen
 * - Service-Aufrufe prüfen
 *
 * @WebMvcTest lädt nur die Web-Schicht,
 * also Controller, JSON-Mapping und MockMvc.
 */
@WebMvcTest(DailyMealController.class)
@AutoConfigureMockMvc(addFilters = false)
class DailyMealControllerTest {

    /**
     * MockMvc simuliert HTTP-Requests,
     * ohne dass ein echter Server gestartet werden muss.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * DailyMealService wird gemockt,
     * weil @WebMvcTest keine echten Service-Beans lädt.
     */
    @MockitoBean
    private DailyMealService dailyMealService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Konstruktor-Test.
     *
     * Erwartung:
     * Es wird eine Instanz von DailyMealController erstellt.
     */
    @Test
    void constructor_createsInstance() {
        DailyMealService mockService = org.mockito.Mockito.mock(DailyMealService.class);
        DailyMealController controller = new DailyMealController(mockService);
        Assertions.assertNotNull(controller);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans ohne start und end.
     *
     * Erwartung:
     * Der Controller ruft getMealPlans(null, null) auf.
     */
    @Test
    void getMealPlansWithoutDateRange_returnsAllMealPlans() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);

        DailyMealResponseDto mealPlan = new DailyMealResponseDto(UUID.randomUUID(), date, null, null, null, 1, 2, 3);

        // Service-Mock vorbereiten: Controller ruft getMealPlans(null, null) auf
        when(dailyMealService.getMealPlans(null, null)).thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/mealplans"))
                .andExpect(status().isOk()) // HTTP 200 erwartet
                .andExpect(jsonPath("$[0].date").value("2026-04-20"));

        // Prüfen, ob der richtige Service-Aufruf passiert ist
        verify(dailyMealService).getMealPlans(null, null);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans mit start und end.
     *
     * Erwartung:
     * Der Controller ruft getMealPlans(start, end) auf.
     */
    @Test
    void getMealPlansWithStartAndEnd_returnsMealPlansBetweenDates() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 20);
        LocalDate end = LocalDate.of(2026, 4, 26);

        DailyMealResponseDto mealPlan = new DailyMealResponseDto(UUID.randomUUID(), start, null, null, null, 1, 2, 3);

        // Mock für Datumsbereich vorbereiten: Controller ruft getMealPlans(start, end) auf
        when(dailyMealService.getMealPlans(start, end)).thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/mealplans").param("start", "2026-04-20").param("end", "2026-04-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-04-20"));

        verify(dailyMealService).getMealPlans(start, end);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans nur mit start.
     *
     * Erwartung laut aktueller Controller-Logik:
     * Der Controller ruft getMealPlans(start, null) auf.
     * Der Service berechnet dann automatisch bis "heute".
     */
    @Test
    void getMealPlansWithOnlyStart_returnsMealPlansBetweenStartAndNow() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 20);
        when(dailyMealService.getMealPlans(start, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/mealplans").param("start", "2026-04-20")).andExpect(status().isOk());

        verify(dailyMealService).getMealPlans(start, null);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans nur mit end.
     *
     * Erwartung laut aktueller Controller-Logik:
     * Der Controller ruft getMealPlans(null, end) auf.
     * Der Service berechnet dann vom ersten verfügbaren Item bis end.
     */
    @Test
    void getMealPlansWithOnlyEnd_returnsMealPlansBetweenFirstAndEnd() throws Exception {
        LocalDate end = LocalDate.of(2026, 4, 26);
        when(dailyMealService.getMealPlans(null, end)).thenReturn(List.of());

        mockMvc.perform(get("/api/mealplans").param("end", "2026-04-26")).andExpect(status().isOk());

        verify(dailyMealService).getMealPlans(null, end);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans/{date}, Plan existiert.
     *
     * Erwartung:
     * HTTP 200 und MealPlan im Response-Body.
     */
    @Test
    void getMealPlan_whenMealPlanExists_returnsOk() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 21);

        DailyMealResponseDto mealPlan = new DailyMealResponseDto(UUID.randomUUID(), date, null, null, null, 1, 1, 1);

        when(dailyMealService.findMealPlanByDate(date)).thenReturn(Optional.of(mealPlan));

        mockMvc.perform(get("/api/mealplans/{date}", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-21"));

        verify(dailyMealService).findMealPlanByDate(date);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans/{date}, Plan existiert nicht.
     *
     * Erwartung:
     * HTTP 404 Not Found.
     */
    @Test
    void getMealPlan_whenMealPlanDoesNotExist_returnsNotFound() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 21);

        when(dailyMealService.findMealPlanByDate(date)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mealplans/{date}", date)).andExpect(status().isNotFound());

        verify(dailyMealService).findMealPlanByDate(date);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans/{date} mit ungültigem Datum.
     *
     * Erwartung:
     * Spring kann "not-a-date" nicht in LocalDate umwandeln.
     * Deshalb wird HTTP 400 Bad Request erwartet.
     */
    @Test
    void getMealPlan_withInvalidDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/mealplans/not-a-date")).andExpect(status().isBadRequest());
    }

    /**
     * Äquivalenzklasse:
     * POST /api/mealplans mit gültigem JSON.
     *
     * Besonderheit:
     * Das Datum muss im Request-Body im DTO vorhanden sein.
     * Der Controller hat nur @PostMapping ohne Pfad-Parameter.
     */
    @Test
    void saveOrUpdateMealPlan_withValidJson_savesWithDtoDate() throws Exception {
        LocalDate dtoDate = LocalDate.of(2026, 4, 22);
        UUID id = UUID.randomUUID();
        UUID breakfastRecipeId = UUID.randomUUID();

        String requestJson =
                """
                {
                  "date": "2026-04-22",
                  "breakfastRecipeId": "%s",
                  "lunchRecipeId": null,
                  "dinnerRecipeId": null,
                  "breakfastServings": 2,
                  "lunchServings": 0,
                  "dinnerServings": 0
                }
                """
                        .formatted(breakfastRecipeId);

        DailyMealResponseDto responseDto = new DailyMealResponseDto(
                id, dtoDate, new RecipeResponseDto(breakfastRecipeId, "Muesli", "", List.of()), null, null, 2, 0, 0);

        when(dailyMealService.saveOrUpdateDailyMeal(any(DailyMealRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/mealplans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-22"))
                .andExpect(jsonPath("$.breakfastServings").value(2));

        verify(dailyMealService).saveOrUpdateDailyMeal(any(DailyMealRequestDto.class));
    }

    /**
     * Äquivalenzklasse:
     * POST /api/mealplans mit ungültigem JSON.
     *
     * Erwartung:
     * JSON kann nicht gelesen werden,
     * deshalb HTTP 400 Bad Request.
     */
    @Test
    void saveOrUpdateMealPlan_withInvalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/mealplans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(dailyMealService, never()).saveOrUpdateDailyMeal(any(DailyMealRequestDto.class));
    }
}
