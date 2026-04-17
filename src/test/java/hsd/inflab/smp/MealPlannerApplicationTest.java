package hsd.inflab.smp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hsd.inflab.smp.service.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class MealPlannerApplicationTest {

    @Autowired
    private MealPlannerService mealPlannerService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RecipeAPIService recipeAPIService;

    @Test
    void contextLoads() {}

    @Test
    void importantBeansAreCreated() {
        assertNotNull(mealPlannerService);
        assertNotNull(passwordService);
        assertNotNull(configService);
        assertNotNull(recipeAPIService);
    }
}
