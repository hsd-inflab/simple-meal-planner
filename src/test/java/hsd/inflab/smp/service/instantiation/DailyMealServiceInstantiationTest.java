package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.service.DailyMealService;
import hsd.inflab.smp.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = DailyMealServiceInstantiationTest.TestContext.class)
class DailyMealServiceInstantiationTest {

    @Autowired
    private DailyMealService dailyMealService;

    @Test
    void dailyMealService_isInstantiated() {
        assertNotNull(dailyMealService);
    }

    @SpringBootConfiguration
    @Import(DailyMealService.class)
    static class TestContext {

        @Bean
        DailyMealRepository dailyMealRepository() {
            return mock(DailyMealRepository.class);
        }

        @Bean
        RecipeRepository recipeRepository() {
            return mock(RecipeRepository.class);
        }

        @Bean
        RecipeService recipeService() {
            return mock(RecipeService.class);
        }
    }
}
