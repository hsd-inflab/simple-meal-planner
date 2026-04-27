package hsd.inflab.smp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.service.PantryService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        // Arrange: PantryItemDto vorbereiten und Mock konfigurieren
        PantryItemDto item = new PantryItemDto(
                UUID.randomUUID(),
                "Tomate",
                Unit.UNIT,
                2.0,
                Category.VEGETABLE,
                LocalDate.of(2026, 4, 25),
                LocalDate.of(2026, 4, 20),
                "Bio",
                1.99);
        when(pantryService.getPantry()).thenReturn(List.of(item));

        // Act: HTTP-GET-Anfrage an den Controller senden
        mockMvc.perform(get("/api/pantry"))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomate"))
                .andExpect(jsonPath("$[0].category").value("VEGETABLE"));
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
        PantryItemDto requestDto = new PantryItemDto(
                null,
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        PantryItemDto responseDto = new PantryItemDto(
                itemId,
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        when(pantryService.addItem(requestDto)).thenReturn(responseDto);

        // Act: HTTP-POST-Anfrage an den Controller senden
        mockMvc.perform(post("/api/pantry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pantry/" + itemId))
                .andExpect(jsonPath("$.id").value(itemId.toString()));
    }

    /**
     * Testet, ob ein Pantry-Item gelöscht wird und die richtige Antwort zurückgibt.
     *
     * Erwartung:
     * - HTTP-Status 204 (No Content)
     * - PantryService.deleteItem wird aufgerufen
     */
    @Test
    void deleteItemReturnsNoContentForExistingItem() throws Exception {
        // Arrange: Testdaten und Mock konfigurieren
        UUID itemId = UUID.randomUUID();
        PantryItemDto item = new PantryItemDto(
                itemId, "Apfel", Unit.UNIT, 4.0, Category.FRUIT, null, LocalDate.of(2026, 4, 20), "Obsthof", 2.50);
        when(pantryService.getItemById(itemId)).thenReturn(Optional.of(item));

        // Act: HTTP-DELETE-Anfrage an den Controller senden
        mockMvc.perform(delete("/api/pantry/{id}", itemId))

                // Assert: Überprüfen der Antwort und der Interaktion mit dem Service
                .andExpect(status().isNoContent());
        verify(pantryService).deleteItem(itemId);
    }
}
