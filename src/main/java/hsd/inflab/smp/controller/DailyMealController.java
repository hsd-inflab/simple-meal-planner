package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.request.DailyMealRequestDto;
import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.service.DailyMealService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mealplans")
public class DailyMealController {
    private final DailyMealService dailyMealService;

    public DailyMealController(DailyMealService dailyMealService) {
        this.dailyMealService = dailyMealService;
    }

    @GetMapping
    public List<DailyMealResponseDto> getMealPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Principal principal) {
        return dailyMealService.getMealPlans(start, end, principal.getName());
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyMealResponseDto> getMealPlan(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Principal principal) {
        return dailyMealService
                .findMealPlanByDate(date, principal.getName())
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping
    public ResponseEntity<DailyMealResponseDto> saveOrUpdateMealPlan(
            @RequestBody DailyMealRequestDto mealPlanRequest, Principal principal) {
        return ResponseEntity.ok(dailyMealService.saveOrUpdateDailyMeal(mealPlanRequest, principal.getName()));
    }
}
