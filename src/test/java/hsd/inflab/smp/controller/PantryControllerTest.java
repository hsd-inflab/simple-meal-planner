package hsd.inflab.smp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.security.JwtAuthenticationFilter;
import hsd.inflab.smp.service.PantryService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testklasse für den PantryController.
 *
 * Ziel:
 * - Controller isoliert testen
 * - HTTP-Statuscodes prüfen
 * - JSON-Antworten prüfen
 * - Service-Aufrufe prüfen
 *
 * @WebMvcTest lädt nur die Web-Schicht,
 * also Controller, JSON-Mapping und MockMvc.
 */
@WebMvcTest(PantryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PantryControllerTest {

    /**
     * MockMvc simuliert HTTP-Requests,
     * ohne dass ein echter Server gestartet werden muss.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * PantryService wird gemockt,
     * weil @WebMvcTest keine echten Service-Beans lädt.
     */
    @MockitoBean
    private PantryService pantryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Konstruktor-Test.
     *
     * Erwartung:
     * Es wird eine Instanz von PantryController erstellt.
     */
    @Test
    void constructor_createsInstance() {
        // Arrange: Mock für PantryService erstellen
        PantryService mockPantryService = org.mockito.Mockito.mock(PantryService.class);

        // Act: PantryController-Instanz erstellen
        PantryController controller = new PantryController(mockPantryService);

        // Assert: Überprüfen, dass die Instanz nicht null ist
        Assertions.assertNotNull(controller);
    }

    /**
     * Testet, ob die Methode getPantry die gespeicherten Pantry-Items zurückgibt.
     *
     * Erwartung:
     * - HTTP-Status 200 (OK)
     * - JSON-Antwort enthält die Pantry-Items
     */
    @Test
    void getPantryReturnsStoredItems() throws Exception {
        // Arrange: PantryItemResponseDto vorbereiten und Mock konfigurieren
        PantryItemResponseDto item = new PantryItemResponseDto(
                UUID.randomUUID(),
                "Tomate",
                Unit.UNIT,
                2.0,
                Category.VEGETABLE,
                LocalDate.of(2026, 4, 25),
                LocalDate.of(2026, 4, 20),
                "Bio",
                1.99);
        when(pantryService.getPantry("pantry-owner")).thenReturn(List.of(item));

        // Act: HTTP-GET-Anfrage an den Controller senden
        mockMvc.perform(get("/api/pantry").principal(() -> "pantry-owner"))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomate"))
                .andExpect(jsonPath("$[0].category").value("VEGETABLE"));
        verify(pantryService).getPantry("pantry-owner");
    }

    /**
     * Testet, ob ein neues Pantry-Item hinzugefügt wird und die richtige Antwort zurückgibt.
     *
     * Erwartung:
     * - HTTP-Status 201 (Created)
     * - Location-Header enthält die URI des neuen Items
     * - JSON-Antwort enthält die ID des neuen Items
     */
    @Test
    void addItemReturnsCreatedResponse() throws Exception {
        // Arrange: Testdaten und Mock konfigurieren
        UUID itemId = UUID.randomUUID();
        String requestJson =
                """
                {
                  \"name\": \"Milch\",
                  \"unit\": \"L\",
                  \"amount\": 1.0,
                  \"category\": \"DAIRY\",
                  \"expirationDate\": \"2026-04-22\",
                  \"purchaseDate\": \"2026-04-20\",
                  \"brand\": \"Marke\",
                  \"price\": 1.49
                }
                """;
        PantryItemRequestDto requestDto = new PantryItemRequestDto(
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        PantryItemResponseDto responseDto = new PantryItemResponseDto(
                itemId,
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        when(pantryService.addItem(requestDto, "pantry-owner")).thenReturn(responseDto);

        // Act: HTTP-POST-Anfrage an den Controller senden
        mockMvc.perform(post("/api/pantry")
                        .principal(() -> "pantry-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pantry/" + itemId))
                .andExpect(jsonPath("$.id").value(itemId.toString()));

        verify(pantryService).addItem(requestDto, "pantry-owner");
    }

    /**
     * Testet, ob deleteExpiredItems die abgelaufenen Items entfernt.
     *
     * Erwartung:
     * - HTTP-Status 204 (No Content)
     * - Service-Methode deleteExpiredItems wird aufgerufen
     */
    @Test
    void deleteExpired_returnsNoContent() throws Exception {
        // Act: HTTP-DELETE-Anfrage an den Controller senden.
        mockMvc.perform(delete("/api/pantry/expired"))

                // Assert: Überprüfen des Statuscodes.
                .andExpect(status().isNoContent());

        // Assert: Service-Aufruf pruefen.
        verify(pantryService).deleteExpiredItems();
    }
}
