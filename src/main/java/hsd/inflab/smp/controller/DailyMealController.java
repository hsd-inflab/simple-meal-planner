package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.DailyMealDto;
import hsd.inflab.smp.service.DailyMealService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
        if (start != null && end != null) {
            return dailyMealService.getMealPlansBetween(start, end);
        }
        return dailyMealService.getAllMealPlans();
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
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody DailyMealDto mealPlanDto) {
        DailyMealDto mealPlanForDate = new DailyMealDto(
                mealPlanDto.id(),
                date,
                mealPlanDto.breakfastRecipe(),
                mealPlanDto.lunchRecipe(),
                mealPlanDto.dinnerRecipe(),
                mealPlanDto.breakfastServings(),
                mealPlanDto.lunchServings(),
                mealPlanDto.dinnerServings());
        return ResponseEntity.ok(dailyMealService.saveOrUpdateDailyMeal(mealPlanForDate));
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteMealPlan(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (dailyMealService.findMealPlanByDate(date).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        dailyMealService.deleteMealPlanByDate(date);
        return ResponseEntity.noContent().build();
    }
}
