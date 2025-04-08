package hsd.inflab.smp;

import services.MealPlannerService;

public class Main {
    public static void main(String[] args) {
        MealPlannerService mealPlanner = new MealPlannerService();
        mealPlanner.startCLILoop();
    }
}
