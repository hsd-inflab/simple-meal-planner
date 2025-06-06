package hsd.inflab.smp;

import java.util.Scanner;

import frontend.MealPlannerCLI;
import frontend.MealPlannerFX;
import frontend.MealPlannerUI;
import javafx.application.Application;
import services.MealPlannerService;

public class Main {
    public static void main(String[] args) {
        MealPlannerUI ui;
        MealPlannerService mealPlanner = new MealPlannerService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Meal Planner! If you wish to use CLI version, press Enter. If you wish to use GUI version, enter anything and press enter.");
        
        //String input = scanner.nextLine();
        
        if (/*input.isEmpty()*/false) {
            ui = new MealPlannerCLI(mealPlanner);    
            ui.start();
        } else {
            MealPlannerFX mealPlannerFX = new MealPlannerFX(mealPlanner);
            MealPlannerFX.launch(MealPlannerFX.class, args);
        }

       

    }
    
}