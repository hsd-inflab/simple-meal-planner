package hsd.inflab.smp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.dto.RecipeIngredientDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.service.RecipeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testklasse für den RecipeController.
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
@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    /**
     * MockMvc simuliert HTTP-Requests,
     * ohne dass ein echter Server gestartet werden muss.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * RecipeService wird gemockt,
     * weil @WebMvcTest keine echten Service-Beans lädt.
     */
    @MockitoBean
    private RecipeService recipeService;

    /**
     * Konstruktor-Test.
     *
     * Erwartung:
     * Es wird eine Instanz von RecipeController erstellt.
     */
    @Test
    void constructor_createsInstance() {
        // Arrange: Mock für RecipeService erstellen
        RecipeService mockRecipeService = org.mockito.Mockito.mock(RecipeService.class);

        // Act: RecipeController-Instanz erstellen
        RecipeController controller = new RecipeController(mockRecipeService);

        // Assert: Überprüfen, dass die Instanz nicht null ist
        Assertions.assertNotNull(controller);
    }

    /**
     * Testet, ob die Methode getAvailableRecipes die gefilterte Rezeptliste zurückgibt.
     *
     * Erwartung:
     * - HTTP-Status 200 (OK)
     * - JSON-Antwort enthält die Rezeptdaten
     */
    @Test
    void getAvailableRecipesReturnsFilteredRecipeList() throws Exception {
        // Arrange: Rezept-Daten vorbereiten und Mock konfigurieren
        RecipeDto recipe = new RecipeDto(
                UUID.randomUUID(),
                "Pasta",
                "Einfach",
                List.of(new RecipeIngredientDto(
                        UUID.randomUUID(), "Nudeln", Unit.G, 100.0, Category.STARCH, "Teigware", "kochen")));
        when(recipeService.getAvailableRecipes()).thenReturn(List.of(recipe));

        // Act: HTTP-GET-Anfrage an den Controller senden
        mockMvc.perform(get("/api/recipes/available"))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pasta"));
    }

    /**
     * Testet, ob ein neues Rezept hinzugefügt wird und die richtige Antwort zurückgibt.
     *
     * Erwartung:
     * - HTTP-Status 201 (Created)
     * - Location-Header enthält die URI des neuen Rezepts
     * - JSON-Antwort enthält die ID des neuen Rezepts
     */
    @Test
    void addRecipeReturnsCreatedRecipe() throws Exception {
        // Arrange: Testdaten und Mock konfigurieren
        UUID recipeId = UUID.randomUUID();
        String requestJson =
                """
                {
                  \"name\": \"Salat\",
                  \"description\": \"Frisch\",
                  \"ingredientsPerPerson\": [
                    {
                      \"name\": \"Gurke\",
                      \"unit\": \"UNIT\",
                      \"amount\": 1.0,
                      \"category\": \"VEGETABLE\",
                      \"foodType\": \"Gemuese\",
                      \"preparation\": \"schneiden\"
                    }
                  ]
                }
                """;
        RecipeDto requestDto = new RecipeDto(
                null,
                "Salat",
                "Frisch",
                List.of(new RecipeIngredientDto(
                        null, "Gurke", Unit.UNIT, 1.0, Category.VEGETABLE, "Gemuese", "schneiden")));
        RecipeDto responseDto =
                new RecipeDto(recipeId, requestDto.name(), requestDto.description(), requestDto.ingredientsPerPerson());
        when(recipeService.addRecipe(requestDto)).thenReturn(responseDto);

        // Act: HTTP-POST-Anfrage an den Controller senden
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                // Assert: Überprüfen der Antwort
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/recipes/" + recipeId))
                .andExpect(jsonPath("$.id").value(recipeId.toString()));
    }
}
