package hsd.inflab.smp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke-Test für die Spring-Boot-Anwendung.
 *
 * Ziel:
 * - Prüfen, ob der Spring ApplicationContext vollständig startet.
 * - Prüfen, ob alle Beans korrekt erzeugt werden können.
 *
 * Hinweis:
 * Dieser Test ist kein Unit-Test, sondern ein Integrationstest,
 * weil mit @SpringBootTest der komplette Spring-Kontext gestartet wird.
 */
@SpringBootTest
@Disabled("Dieser Test ist ein Smoke-Test und wird nur ausgeführt, wenn der Kontext-Start überprüft werden soll.")
@ActiveProfiles("test") // Aktiviert application-test.properties aus src/test/resources
class MealPlannerApplicationTest {

    /**
     * Dieser Test bleibt absichtlich leer.
     *
     * Wenn der Spring Context nicht geladen werden kann,
     * schlägt der Test automatisch fehl.
     */
    @Test
    void contextLoads() {
        // Kein Code nötig:
        // Der Test ist erfolgreich, wenn die Anwendung ohne Fehler startet.
    }
}
