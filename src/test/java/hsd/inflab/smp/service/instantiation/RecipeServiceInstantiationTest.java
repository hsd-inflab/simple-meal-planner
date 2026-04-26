package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = RecipeServiceInstantiationTest.TestContext.class)
class RecipeServiceInstantiationTest {

    @Autowired
    private RecipeService recipeService;

    @Test
    void recipeService_isInstantiated() {
        assertNotNull(recipeService);
    }

    @SpringBootConfiguration
    @Import(RecipeService.class)
    static class TestContext {

        @Bean
        RecipeRepository recipeRepository() {
            return mock(RecipeRepository.class);
        }

        @Bean
        PantryItemRepository pantryItemRepository() {
            return mock(PantryItemRepository.class);
        }
    }
}
