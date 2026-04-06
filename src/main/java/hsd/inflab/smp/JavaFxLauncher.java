package hsd.inflab.smp;

import hsd.inflab.smp.frontend.MealPlannerFX;
import javafx.application.Application;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxLauncher {

    public static void main(String[] args) {

        // 'context' is used to access Spring Beans in the JavaFX application
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(MealPlannerApplication.class).run(args);

        MealPlannerFX.setApplicationContext(context);

        Application.launch(MealPlannerFX.class, args);
    }
}