package hsd.inflab.smp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MealPlannerApplication {

    static {
        Dotenv dotenv = Dotenv.configure().directory(".").ignoreIfMissing().load();

        // Populate System properties from ...env
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {
        SpringApplication.run(MealPlannerApplication.class, args);
    }
}
