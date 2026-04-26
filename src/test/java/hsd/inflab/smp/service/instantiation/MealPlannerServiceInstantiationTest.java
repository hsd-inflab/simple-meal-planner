package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import hsd.inflab.smp.service.ConfigService;
import hsd.inflab.smp.service.DailyMealService;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.service.PantryService;
import hsd.inflab.smp.service.PasswordService;
import hsd.inflab.smp.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = MealPlannerServiceInstantiationTest.TestContext.class)
class MealPlannerServiceInstantiationTest {

    @Autowired
    private MealPlannerService mealPlannerService;

    @Test
    void mealPlannerService_isInstantiated() {
        assertNotNull(mealPlannerService);
    }

    @SpringBootConfiguration
    @Import(MealPlannerService.class)
    static class TestContext {

        @Bean
        ConfigService configService() {
            return mock(ConfigService.class);
        }

        @Bean
        PasswordService passwordService() {
            return mock(PasswordService.class);
        }

        @Bean
        PantryService pantryService() {
            return mock(PantryService.class);
        }

        @Bean
        RecipeService recipeService() {
            return mock(RecipeService.class);
        }

        @Bean
        DailyMealService dailyMealService() {
            return mock(DailyMealService.class);
        }
    }
}
