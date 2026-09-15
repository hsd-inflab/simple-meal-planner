package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Verifies WHEN the starter data bean is created, not what it does. Unlike the autofiller this feature is on by
 * default: the data belongs to the user who just registered, so switching it off is an explicit choice.
 */
class UserStarterDataServiceRegistrationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestDependencies.class, UserStarterDataService.class);

    @Test
    void starterData_isRegistered_whenPropertyIsAbsent() {
        // arrange: no property at all -> the default of a fresh environment

        // act & assert
        contextRunner.run(context -> assertThat(context).hasSingleBean(UserStarterDataService.class));
    }

    @Test
    void starterData_isNotRegistered_whenPropertyIsFalse() {
        // arrange
        ApplicationContextRunner runner = contextRunner.withPropertyValues("app.starter-data.enabled=false");

        // act & assert
        runner.run(context -> assertThat(context).doesNotHaveBean(UserStarterDataService.class));
    }

    @Test
    void starterData_isRegistered_whenPropertyIsTrue() {
        // arrange
        ApplicationContextRunner runner = contextRunner.withPropertyValues("app.starter-data.enabled=true");

        // act & assert
        runner.run(context -> assertThat(context).hasSingleBean(UserStarterDataService.class));
    }

    @Configuration
    static class TestDependencies {

        @Bean
        PantryItemRepository pantryItemRepository() {
            return Mockito.mock(PantryItemRepository.class);
        }

        @Bean
        DailyMealRepository dailyMealRepository() {
            return Mockito.mock(DailyMealRepository.class);
        }

        @Bean
        RecipeRepository recipeRepository() {
            return Mockito.mock(RecipeRepository.class);
        }
    }
}
