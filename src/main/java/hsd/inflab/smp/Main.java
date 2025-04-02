package hsd.inflab.smp;

import services.MealPlannerService;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        MealPlannerService mealPlanner = new MealPlannerService();
        mealPlanner.startCLILoop();
    }
}