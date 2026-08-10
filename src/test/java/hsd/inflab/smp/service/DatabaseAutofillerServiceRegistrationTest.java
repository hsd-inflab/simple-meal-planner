package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifies WHEN the autofiller bean is created, not what it does.
 * The guard must fail closed: no explicit opt-in means no bean.
 */
class DatabaseAutofillerServiceRegistrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestDependencies.class, DatabaseAutofillerService.class);

    @Test
    void autofiller_isNotRegistered_whenPropertyIsAbsent() {
        // arrange: no property at all -> the default of a fresh environment

        // act & assert
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DatabaseAutofillerService.class));
    }

    @Test
    void autofiller_isNotRegistered_whenPropertyIsFalse() {
        // arrange
        ApplicationContextRunner runner = contextRunner.withPropertyValues("app.autofill.enabled=false");

        // act & assert
        runner.run(context -> assertThat(context).doesNotHaveBean(DatabaseAutofillerService.class));
    }

    @Test
    void autofiller_isRegistered_whenPropertyIsTrue() {
        // arrange
        ApplicationContextRunner runner = contextRunner.withPropertyValues("app.autofill.enabled=true");

        // act & assert
        runner.run(context -> assertThat(context).hasSingleBean(DatabaseAutofillerService.class));
    }

    @Test
    void autofiller_isNotRegistered_whenProdProfileIsActive_evenIfPropertyIsTrue() {
        // arrange: the flag was set by mistake in production
        ApplicationContextRunner runner =
                contextRunner.withPropertyValues("app.autofill.enabled=true", "spring.profiles.active=prod");

        // act & assert
        runner.run(context -> assertThat(context).doesNotHaveBean(DatabaseAutofillerService.class));
    }

    @Test
    void applicationProperties_shipAutofillDisabledByDefault() throws Exception {
        // arrange
        Properties properties = new Properties();

        // act
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            properties.load(in);
        }

        // assert
        assertThat(properties.getProperty("app.autofill.enabled")).isEqualTo("false");
    }

    @Configuration
    static class TestDependencies {

        @Bean
        JdbcTemplate jdbcTemplate() {
            return Mockito.mock(JdbcTemplate.class);
        }

        @Bean
        DailyMealRepository dailyMealRepository() {
            return Mockito.mock(DailyMealRepository.class);
        }

        @Bean
        PantryItemRepository pantryItemRepository() {
            return Mockito.mock(PantryItemRepository.class);
        }

        @Bean
        RecipeRepository recipeRepository() {
            return Mockito.mock(RecipeRepository.class);
        }
    }
}
