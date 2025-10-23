package hsd.inflab.smp;

import frontend.MealPlannerFX;
import services.MealPlannerService;

public class Main {
    public static void main(String[] args) {
        MealPlannerService mealPlanner = new MealPlannerService();
        MealPlannerFX mealPlannerFX = new MealPlannerFX(mealPlanner);
        MealPlannerFX.launch(MealPlannerFX.class, args);
    }
    
}