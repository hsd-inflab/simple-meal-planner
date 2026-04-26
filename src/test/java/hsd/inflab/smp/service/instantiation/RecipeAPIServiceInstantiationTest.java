package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hsd.inflab.smp.service.ConfigService;
import hsd.inflab.smp.service.RecipeAPIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = RecipeAPIServiceInstantiationTest.TestContext.class)
class RecipeAPIServiceInstantiationTest {

    @Autowired
    private RecipeAPIService recipeAPIService;

    @Test
    void recipeApiService_isInstantiated() {
        assertNotNull(recipeAPIService);
    }

    @SpringBootConfiguration
    @Import(RecipeAPIService.class)
    static class TestContext {

        @Bean
        ConfigService configService() {
            return new ConfigService();
        }
    }
}
