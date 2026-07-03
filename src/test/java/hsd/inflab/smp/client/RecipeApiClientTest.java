// Testklasse für RecipeApiClient
// Ziel: Überprüft, ob der Client HTTP-Anfragen korrekt stellt und auf verschiedene API-Antworten richtig reagiert.
// - Nutzt MockWebServer zur Simulation der API
// - Prüft Header, Pfad und Fehlerbehandlung
// - Verwendet Mockito für Konfigurationswerte

package hsd.inflab.smp.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.service.ConfigService;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeApiClientTest {

    // Mock für Konfigurationsservice (liefert API-Base, Key, Host)
    @Mock
    private ConfigService configService;

    // Simulierter HTTP-Server für API-Requests
    private MockWebServer mockWebServer;

    // Zu testender Client
    private RecipeApiClient recipeApiClient;

    @BeforeEach
    void setUp() throws IOException {
        // Starte MockWebServer vor jedem Test
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String mockBaseUrl = mockWebServer.url("/").toString();

        // Konfiguriere Mock-Verhalten für ConfigService
        when(configService.getRecipeApiBase()).thenReturn(mockBaseUrl);
        when(configService.getRecipeApiKey()).thenReturn("test-key");
        when(configService.getRecipeApiHost()).thenReturn("test-host");

        // Initialisiere Client mit gemocktem ConfigService
        recipeApiClient = new RecipeApiClient(configService);

        System.out.println("Mock API gestartet unter: " + mockBaseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Stoppe MockWebServer nach jedem Test
        mockWebServer.shutdown();
        System.out.println("Mock API beendet.");
    }

    @Test
    void searchRecipes_whenApiAvailable_doesNotThrowException() throws InterruptedException {
        // Simuliere erfolgreiche API-Antwort (200 OK, leeres Array)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[]"));

        // Erwartung: Keine Exception bei erfolgreicher Antwort
        assertDoesNotThrow(() -> recipeApiClient.searchRecipes("Apfelkuchen"));

        // Prüfe, ob Request korrekt gestellt wurde
        RecordedRequest request = mockWebServer.takeRequest();

        System.out.println("searchRecipes verfügbar:");
        System.out.println("HTTP-Methode: " + request.getMethod());
        System.out.println("Angefragter Pfad: " + request.getPath());
        System.out.println("API-Key Header: " + request.getHeader("X-RapidAPI-Key"));
        System.out.println("API-Host Header: " + request.getHeader("X-RapidAPI-Host"));

        // Erwartete HTTP-Methode und Pfad prüfen
        assertEquals("GET", request.getMethod());
        assertEquals("/search_api?text=Apfelkuchen", request.getPath());
    }

    @Test
    void searchRecipes_whenApiUnavailable_throwsException() throws InterruptedException {
        // Simuliere Fehler-Antwort (503 Service Unavailable)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        // Erwartung: RuntimeException bei Fehler
        assertThrows(RuntimeException.class, () -> recipeApiClient.searchRecipes("Apfelkuchen"));

        // Prüfe, ob Request korrekt gestellt wurde
        RecordedRequest request = mockWebServer.takeRequest();

        System.out.println("searchRecipes nicht verfügbar:");
        System.out.println("HTTP-Methode: " + request.getMethod());
        System.out.println("Angefragter Pfad: " + request.getPath());

        assertEquals("GET", request.getMethod());
        assertEquals("/search_api?text=Apfelkuchen", request.getPath());
    }

    @Test
    void crawlRecipe_whenApiAvailable_doesNotThrowException() throws InterruptedException {
        // Simuliere erfolgreiche Antwort mit Rezeptanweisungen
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(
                                """
                        {
                          "instructions": ["Step 1", "Step 2"]
                        }
                        """));

        // Erwartung: Keine Exception bei erfolgreicher Antwort
        assertDoesNotThrow(() -> recipeApiClient.crawlRecipe("https://example.com/recipe"));

        // Prüfe, ob Request korrekt gestellt wurde
        RecordedRequest request = mockWebServer.takeRequest();

        System.out.println("crawlRecipe verfügbar:");
        System.out.println("HTTP-Methode: " + request.getMethod());
        System.out.println("Angefragter Pfad: " + request.getPath());
        System.out.println("API-Key Header: " + request.getHeader("X-RapidAPI-Key"));
        System.out.println("API-Host Header: " + request.getHeader("X-RapidAPI-Host"));

        // Erwartete HTTP-Methode und Pfad prüfen
        assertEquals("GET", request.getMethod());
        assertEquals("/crawl?target_url=https://example.com/recipe", request.getPath());
    }

    @Test
    void crawlRecipe_whenApiUnavailable_throwsException() throws InterruptedException {
        // Simuliere Fehler-Antwort (503 Service Unavailable)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        // Erwartung: RuntimeException bei Fehler
        assertThrows(RuntimeException.class, () -> recipeApiClient.crawlRecipe("https://example.com/recipe"));

        // Prüfe, ob Request korrekt gestellt wurde
        RecordedRequest request = mockWebServer.takeRequest();

        System.out.println("crawlRecipe nicht verfügbar:");
        System.out.println("HTTP-Methode: " + request.getMethod());
        System.out.println("Angefragter Pfad: " + request.getPath());

        assertEquals("GET", request.getMethod());
        assertEquals("/crawl?target_url=https://example.com/recipe", request.getPath());
    }
}
