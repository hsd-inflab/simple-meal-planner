package hsd.inflab.smp;

import hsd.inflab.smp.frontend.MealPlannerFX;
import javafx.application.Application;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxLauncher {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(MealPlannerApplication.class).run(args);

        MealPlannerFX.setApplicationContext(context);

        Application.launch(MealPlannerFX.class, args);
    }
}