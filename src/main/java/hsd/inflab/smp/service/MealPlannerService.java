package hsd.inflab.smp.service;

import org.springframework.stereotype.Service;

/**
 * main service, provides access and stores all objects/lists used at runtime
 */
@Service
public class MealPlannerService {
    private final ConfigService configService;
    private final PasswordService passwordService;
    public final PantryService pantryService;
    public final RecipeService recipeService;
    public final DailyMealService dailyMealService;

    public MealPlannerService(
            ConfigService configService,
            PasswordService passwordService,
            PantryService pantryService,
            RecipeService recipeService,
            DailyMealService dailyMealService) {
        this.configService = configService;
        this.passwordService = passwordService;
        this.pantryService = pantryService;
        this.recipeService = recipeService;
        this.dailyMealService = dailyMealService;
    }

    public boolean verifyAPIPassword(String password) {
        return passwordService.verifyPassword(password, configService.getRecipeApiPasswordhash());
    }
}
