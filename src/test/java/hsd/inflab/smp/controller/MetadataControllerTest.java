package hsd.inflab.smp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.response.EnumOptionDto;
import hsd.inflab.smp.dto.response.MetadataResponseDto;
import hsd.inflab.smp.security.JwtAuthenticationFilter;
import hsd.inflab.smp.service.MetadataService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testklasse fuer den MetadataController.
 *
 * Ziel:
 * - Controller isoliert testen (nur Web-Schicht via @WebMvcTest).
 * - HTTP-Status und JSON-Struktur der Metadaten-Antwort pruefen.
 *
 * Der MetadataService wird gemockt, damit nur das Web-Mapping getestet wird.
 */
@WebMvcTest(MetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetadataService metadataService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Erwartung:
     * - HTTP-Status 200 (OK).
     * - JSON enthaelt units und categories jeweils mit value und displayName.
     */
    @Test
    void getMetadata_returnsUnitsAndCategories() throws Exception {
        // Arrange: Service liefert je einen Beispiel-Eintrag zurueck.
        MetadataResponseDto metadata = new MetadataResponseDto(
                List.of(new EnumOptionDto("ML", "ml")), List.of(new EnumOptionDto("MEAT", "Fleisch")));
        when(metadataService.getMetadata()).thenReturn(metadata);

        // Act: HTTP-GET-Anfrage an den Controller senden.
        mockMvc.perform(get("/api/metadata"))

                // Assert: Status und JSON-Struktur pruefen.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.units[0].value").value("ML"))
                .andExpect(jsonPath("$.units[0].displayName").value("ml"))
                .andExpect(jsonPath("$.categories[0].value").value("MEAT"))
                .andExpect(jsonPath("$.categories[0].displayName").value("Fleisch"));
    }

    /**
     * Erwartung:
     * - HTTP-Status 200 (OK).
     * - JSON ist eine Liste von Units mit value und displayName.
     */
    @Test
    void getUnits_returnsUnitList() throws Exception {
        // Arrange: Service liefert einen Beispiel-Unit-Eintrag zurueck.
        when(metadataService.getUnits()).thenReturn(List.of(new EnumOptionDto("ML", "ml")));

        // Act: HTTP-GET-Anfrage an den Units-Endpoint senden.
        mockMvc.perform(get("/api/metadata/units"))

                // Assert: Status und JSON-Struktur pruefen.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("ML"))
                .andExpect(jsonPath("$[0].displayName").value("ml"));
    }

    /**
     * Erwartung:
     * - HTTP-Status 200 (OK).
     * - JSON ist eine Liste von Categories mit value und displayName.
     */
    @Test
    void getCategories_returnsCategoryList() throws Exception {
        // Arrange: Service liefert einen Beispiel-Category-Eintrag zurueck.
        when(metadataService.getCategories()).thenReturn(List.of(new EnumOptionDto("MEAT", "Fleisch")));

        // Act: HTTP-GET-Anfrage an den Categories-Endpoint senden.
        mockMvc.perform(get("/api/metadata/categories"))

                // Assert: Status und JSON-Struktur pruefen.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("MEAT"))
                .andExpect(jsonPath("$[0].displayName").value("Fleisch"));
    }
}
