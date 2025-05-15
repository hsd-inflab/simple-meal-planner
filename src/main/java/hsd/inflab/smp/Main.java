package hsd.inflab.smp;

import java.util.Scanner;

import frontend.MealPlannerCLI;
import frontend.MealPlannerUI;
import services.MealPlannerService;

public class Main {
    public static void main(String[] args) {
        MealPlannerUI ui;
        MealPlannerService mealPlanner = new MealPlannerService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Meal Planner! If you wish to use CLI version, press Enter. If you wish to use GUI version, enter anything and press enter.");
        
        String input = scanner.nextLine();
        
        if (input.isEmpty()) {
            ui = new MealPlannerCLI(mealPlanner);    
        } else {
            //TBD: Implement GUI version
            System.out.println("GUI version is not implemented yet.");
            return;
        }

        ui.start();

    }
    
}