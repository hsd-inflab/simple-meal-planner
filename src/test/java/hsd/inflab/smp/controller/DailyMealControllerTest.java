package hsd.inflab.smp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.service.DailyMealService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DailyMealControllerTest {
    private MockMvc mockMvc;
    private DailyMealService dailyMealService;

    @BeforeEach
    void setUp() {
        dailyMealService = Mockito.mock(DailyMealService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DailyMealController(dailyMealService))
                .build();
    }

    @Test
    void getMealPlansDelegatesFilterParametersToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 4, 20);
        LocalDate end = LocalDate.of(2026, 4, 26);
        DailyMealDto mealPlan = new DailyMealDto(UUID.randomUUID(), start, null, null, null, 1, 2, 3);
        when(dailyMealService.getMealPlans(start, end)).thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/mealplans").param("start", "2026-04-20").param("end", "2026-04-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-04-20"));

        verify(dailyMealService).getMealPlans(start, end);
    }

    @Test
    void saveOrUpdateMealPlanUsesDateFromRequestBody() throws Exception {
        LocalDate pathDate = LocalDate.of(2026, 4, 21);
        LocalDate bodyDate = LocalDate.of(2026, 4, 20);
        UUID requestId = UUID.randomUUID();
        String requestJson =
                """
                {
                  "id": "%s",
                  "date": "2026-04-20",
                  "breakfastRecipe": {
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
                        .formatted(requestId);
        DailyMealDto requestDto = new DailyMealDto(
                requestId, bodyDate, new RecipeDto(null, "Muesli", "", List.of()), null, null, 2, 0, 0);
        DailyMealDto responseDto = new DailyMealDto(
                requestDto.id(),
                bodyDate,
                requestDto.breakfastRecipe(),
                requestDto.lunchRecipe(),
                requestDto.dinnerRecipe(),
                2,
                0,
                0);
        when(dailyMealService.saveOrUpdateDailyMeal(any(DailyMealDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/mealplans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-20"));

        ArgumentCaptor<DailyMealDto> captor = ArgumentCaptor.forClass(DailyMealDto.class);
        verify(dailyMealService).saveOrUpdateDailyMeal(captor.capture());
        Assertions.assertEquals(bodyDate, captor.getValue().date());
    }
}
