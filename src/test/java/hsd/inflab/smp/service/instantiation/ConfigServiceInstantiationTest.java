package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hsd.inflab.smp.service.ConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Isolierter Spring-Boot-Test: Wir laden nur den ConfigService ueber einen kleinen Testkontext.
@SpringBootTest(
        classes = ConfigServiceInstantiationTest.TestContext.class,
        properties = {
            // Dateien fuer @Value-Injection im ConfigService
            "pantry.file=test-pantry.json",
            "recipebook.file=test-recipebook.json",
            "mealplans.file=test-mealplans.json",
            "temp.file=test-temp.json",
            // API-bezogene Platzhalterwerte, damit der Bean-Start ohne echte Secrets klappt
            "recipe.api.key=test-api-key",
            "recipe.api.base=https://example.com/search?text=", // Such-Endpoint fuer Tests
            "recipe.crawl.base=https://example.com/crawl?target_url=", // Crawl-Endpoint fuer Tests
            "recipe.api.host=example.com", // Hostname fuer Header-Aufbau
            "recipe.api.passwordhash=test-password-hash"
        })
class ConfigServiceInstantiationTest {

    // Erwartete Spring-Bean: wird beim Kontextstart injiziert.
    @Autowired
    private ConfigService configService;

    @Test
    void configService_isInstantiated() {
        // Smoke-Check: Bean wurde erfolgreich erstellt.
        assertNotNull(configService);
    }

    @SpringBootConfiguration
    @Import(ConfigService.class)
    static class TestContext {
        // Keine weiteren Beans noetig; ConfigService reicht fuer diesen Testfall.
    }
}
