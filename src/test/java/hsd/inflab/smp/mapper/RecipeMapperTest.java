package hsd.inflab.smp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hsd.inflab.smp.dto.request.RecipeIngredientRequestDto;
import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeIngredientResponseDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeMapperTest {

    private final RecipeMapper mapper = new RecipeMapperImpl();

    @Test
    void toDto_mapsRecipeAndIngredients() {
        RecipeIngredient ingredient = new RecipeIngredient("Tomato", Unit.G, 200.0, Category.VEGETABLE, "veg", "diced");
        Recipe recipe = new Recipe("Pasta", "Simple pasta", List.of(ingredient));

        RecipeResponseDto result = mapper.toDto(recipe);

        // id der Entity wird (mangels Persistenz) nicht gesetzt
        assertNull(result.id());
        assertEquals("Pasta", result.name());
        assertEquals("Simple pasta", result.description());
        assertEquals(1, result.ingredientsPerPerson().size());

        RecipeIngredientResponseDto mapped = result.ingredientsPerPerson().getFirst();
        assertNull(mapped.id());
        assertEquals("Tomato", mapped.name());
        assertEquals(Unit.G, mapped.unit());
        assertEquals(200.0, mapped.amount());
        assertEquals(Category.VEGETABLE, mapped.category());
        assertEquals("veg", mapped.foodType());
        assertEquals("diced", mapped.preparation());
    }

    @Test
    void toEntity_mapsFieldsAndIngredients_withoutId() {
        RecipeIngredientRequestDto ingredientDto =
                new RecipeIngredientRequestDto("Milk", Unit.L, 1.0, Category.DAIRY, "dairy", "fresh");
        RecipeRequestDto dto = new RecipeRequestDto("Porridge", "Breakfast", List.of(ingredientDto));

        Recipe result = mapper.toEntity(dto);

        assertNull(result.getId());
        assertEquals("Porridge", result.getName());
        assertEquals("Breakfast", result.getDescription());
        assertEquals(1, result.getIngredientsPerPerson().size());
        assertEquals("Milk", result.getIngredientsPerPerson().getFirst().getName());
    }

    @Test
    void toEntity_withNullIngredients_yieldsEmptyList() {
        RecipeRequestDto dto = new RecipeRequestDto("Leer", "Ohne Zutaten", null);

        Recipe result = mapper.toEntity(dto);

        assertEquals("Leer", result.getName());
        assertTrue(result.getIngredientsPerPerson().isEmpty());
    }

    @Test
    void mappingNullReturnsNull() {
        assertNull(mapper.toDto((Recipe) null));
        assertNull(mapper.toEntity((RecipeRequestDto) null));
    }
}
