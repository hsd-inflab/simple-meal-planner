package hsd.inflab.smp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecipeControllerTest {
    private MockMvc mockMvc;
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = Mockito.mock(RecipeService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RecipeController(recipeService))
                .build();
    }

    @Test
    void getAvailableRecipesReturnsFilteredRecipeList() throws Exception {
        RecipeDto recipe = new RecipeDto(
                UUID.randomUUID(),
                "Pasta",
                "Einfach",
                List.of(new RecipeIngredientDto(
                        UUID.randomUUID(), "Nudeln", Unit.G, 100.0, Category.STARCH, "Teigware", "kochen")));
        when(recipeService.getAvailableRecipes()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pasta"));
    }

    @Test
    void addRecipeReturnsCreatedRecipe() throws Exception {
        UUID recipeId = UUID.randomUUID();
        String requestJson =
                """
                {
                  "name": "Salat",
                  "description": "Frisch",
                  "ingredientsPerPerson": [
                    {
                      "name": "Gurke",
                      "unit": "UNIT",
                      "amount": 1.0,
                      "category": "VEGETABLE",
                      "foodType": "Gemuese",
                      "preparation": "schneiden"
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

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/recipes/" + recipeId))
                .andExpect(jsonPath("$.id").value(recipeId.toString()));
    }

    @Test
    void deleteRecipeReturnsNoContentForExistingRecipe() throws Exception {
        UUID recipeId = UUID.randomUUID();
        RecipeDto recipe = new RecipeDto(recipeId, "Suppe", "Warm", List.of());
        when(recipeService.getRecipeById(recipeId)).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/{id}", recipeId)).andExpect(status().isNoContent());

        verify(recipeService).deleteRecipe(recipeId);
    }
}
