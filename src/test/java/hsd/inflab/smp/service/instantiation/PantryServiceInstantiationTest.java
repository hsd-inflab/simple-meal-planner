package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.service.PantryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = PantryServiceInstantiationTest.TestContext.class)
class PantryServiceInstantiationTest {

    @Autowired
    private PantryService pantryService;

    @Test
    void pantryService_isInstantiated() {
        assertNotNull(pantryService);
    }

    @SpringBootConfiguration
    @Import(PantryService.class)
    static class TestContext {

        @Bean
        PantryItemRepository pantryItemRepository() {
            return mock(PantryItemRepository.class);
        }
    }
}
