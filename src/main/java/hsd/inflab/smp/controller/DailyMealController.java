package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.service.DailyMealService;
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
    public List<DailyMealDto> getMealPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return dailyMealService.getMealPlans(start, end);
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyMealDto> getMealPlan(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyMealService
                .findMealPlanByDate(date)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping("/{date}")
    public ResponseEntity<DailyMealDto> saveOrUpdateMealPlan(
            @RequestBody DailyMealDto mealPlanDto) {
        return ResponseEntity.ok(dailyMealService.saveOrUpdateDailyMeal(mealPlanDto));
    }
}
