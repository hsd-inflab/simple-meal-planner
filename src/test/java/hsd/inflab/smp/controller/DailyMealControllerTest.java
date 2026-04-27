package hsd.inflab.smp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.service.DailyMealService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Der Controller ruft getAllMealPlans() auf.
     */
    @Test
    void getMealPlansWithoutDateRange_returnsAllMealPlans() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);

        DailyMealDto mealPlan = new DailyMealDto(UUID.randomUUID(), date, null, null, null, 1, 2, 3);

        // Service-Mock vorbereiten
        when(dailyMealService.getAllMealPlans()).thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/mealplans"))
                .andExpect(status().isOk()) // HTTP 200 erwartet
                .andExpect(jsonPath("$[0].date").value("2026-04-20"));

        // Prüfen, ob der richtige Service-Aufruf passiert ist
        verify(dailyMealService).getAllMealPlans();
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans mit start und end.
     *
     * Erwartung:
     * Der Controller ruft getMealPlansBetween(start, end) auf.
     */
    @Test
    void getMealPlansWithStartAndEnd_returnsMealPlansBetweenDates() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 20);
        LocalDate end = LocalDate.of(2026, 4, 26);

        DailyMealDto mealPlan = new DailyMealDto(UUID.randomUUID(), start, null, null, null, 1, 2, 3);

        // Mock für Datumsbereich vorbereiten
        when(dailyMealService.getMealPlansBetween(start, end)).thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/mealplans").param("start", "2026-04-20").param("end", "2026-04-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-04-20"));

        verify(dailyMealService).getMealPlansBetween(start, end);
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans nur mit start.
     *
     * Erwartung laut aktueller Controller-Logik:
     * Da nicht beide Parameter vorhanden sind,
     * wird getAllMealPlans() aufgerufen.
     */
    @Test
    void getMealPlansWithOnlyStart_returnsAllMealPlans() throws Exception {
        when(dailyMealService.getAllMealPlans()).thenReturn(List.of());

        mockMvc.perform(get("/api/mealplans").param("start", "2026-04-20")).andExpect(status().isOk());

        verify(dailyMealService).getAllMealPlans();
        verify(dailyMealService, never()).getMealPlansBetween(any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * Äquivalenzklasse:
     * GET /api/mealplans nur mit end.
     *
     * Erwartung laut aktueller Controller-Logik:
     * Da nicht beide Parameter vorhanden sind,
     * wird getAllMealPlans() aufgerufen.
     */
    @Test
    void getMealPlansWithOnlyEnd_returnsAllMealPlans() throws Exception {
        when(dailyMealService.getAllMealPlans()).thenReturn(List.of());

        mockMvc.perform(get("/api/mealplans").param("end", "2026-04-26")).andExpect(status().isOk());

        verify(dailyMealService).getAllMealPlans();
        verify(dailyMealService, never()).getMealPlansBetween(any(LocalDate.class), any(LocalDate.class));
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

        DailyMealDto mealPlan = new DailyMealDto(UUID.randomUUID(), date, null, null, null, 1, 1, 1);

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
     * POST /api/mealplans/{date} mit gültigem JSON.
     *
     * Besonderheit:
     * Das Datum aus der URL soll verwendet werden,
     * auch wenn im JSON-Body ein anderes Datum steht.
     */
    @Test
    void saveOrUpdateMealPlan_withValidJson_usesDateFromPath() throws Exception {
        LocalDate pathDate = LocalDate.of(2026, 4, 22);
        UUID id = UUID.randomUUID();

        String requestJson =
                """
                {
                  "id": "%s",
                  "date": "2026-04-20",
                  "breakfastRecipe": {
                    "id": null,
                    "name": "Muesli",
                    "description": "",
                    "ingredientsPerPerson": []
                  },
                  "lunchRecipe": null,
                  "dinnerRecipe": null,
                  "breakfastServings": 2,
                  "lunchServings": 0,
                  "dinnerServings": 0
                }
                """
                        .formatted(id);

        DailyMealDto responseDto =
                new DailyMealDto(id, pathDate, new RecipeDto(null, "Muesli", "", List.of()), null, null, 2, 0, 0);

        when(dailyMealService.saveOrUpdateDailyMeal(any(DailyMealDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/mealplans/{date}", pathDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-22"));

        // ArgumentCaptor speichert das Objekt,
        // das tatsächlich an den Service übergeben wurde.
        ArgumentCaptor<DailyMealDto> captor = ArgumentCaptor.forClass(DailyMealDto.class);

        verify(dailyMealService).saveOrUpdateDailyMeal(captor.capture());

        // Prüft, ob wirklich das Datum aus der URL verwendet wurde
        Assertions.assertEquals(pathDate, captor.getValue().date());
    }

    /**
     * Äquivalenzklasse:
     * POST /api/mealplans/{date} mit ungültigem JSON.
     *
     * Erwartung:
     * JSON kann nicht gelesen werden,
     * deshalb HTTP 400 Bad Request.
     */
    @Test
    void saveOrUpdateMealPlan_withInvalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/mealplans/2026-04-22")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(dailyMealService, never()).saveOrUpdateDailyMeal(any(DailyMealDto.class));
    }

    /**
     * Äquivalenzklasse:
     * DELETE /api/mealplans/{date}, Plan existiert.
     *
     * Erwartung:
     * Plan wird gelöscht und HTTP 204 No Content zurückgegeben.
     */
    @Test
    void deleteMealPlan_whenMealPlanExists_returnsNoContent() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 23);

        DailyMealDto mealPlan = new DailyMealDto(UUID.randomUUID(), date, null, null, null, 1, 1, 1);

        when(dailyMealService.findMealPlanByDate(date)).thenReturn(Optional.of(mealPlan));

        mockMvc.perform(delete("/api/mealplans/{date}", date)).andExpect(status().isNoContent());

        verify(dailyMealService).findMealPlanByDate(date);
        verify(dailyMealService).deleteMealPlanByDate(date);
    }

    /**
     * Äquivalenzklasse:
     * DELETE /api/mealplans/{date}, Plan existiert nicht.
     *
     * Erwartung:
     * HTTP 404 Not Found.
     * Die delete-Methode darf nicht aufgerufen werden.
     */
    @Test
    void deleteMealPlan_whenMealPlanDoesNotExist_returnsNotFound() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 23);

        when(dailyMealService.findMealPlanByDate(date)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/mealplans/{date}", date)).andExpect(status().isNotFound());

        verify(dailyMealService).findMealPlanByDate(date);

        // Wichtig: Es darf nicht gelöscht werden,
        // wenn kein MealPlan existiert.
        verify(dailyMealService, never()).deleteMealPlanByDate(date);
    }

    /**
     * Äquivalenzklasse:
     * DELETE /api/mealplans/{date} mit ungültigem Datum.
     *
     * Erwartung:
     * Spring kann den Pfadwert nicht in LocalDate konvertieren.
     * Deshalb HTTP 400 Bad Request.
     */
    @Test
    void deleteMealPlan_withInvalidDate_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/mealplans/not-a-date")).andExpect(status().isBadRequest());
    }
}
