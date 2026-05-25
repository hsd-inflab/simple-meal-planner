package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        pantryItemRepository.deleteAll();
    }

    @Test
    void findAvailableRecipes_returnsOnlyRecipesWithEnoughPantryItems() {
        RecipeIngredient tomato = new RecipeIngredient("TOMATO", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        RecipeIngredient cheese = new RecipeIngredient("Cheese", Unit.G, 50.0, Category.DAIRY, "dairy", "grated");
        Recipe availableRecipe = new Recipe("Salad", "Can be cooked", List.of(tomato));
        Recipe unavailableRecipe = new Recipe("Pizza", "Cannot be cooked", List.of(cheese));

        recipeRepository.saveAll(List.of(availableRecipe, unavailableRecipe));
        pantryItemRepository.save(new PantryItem("tomato", Unit.G, 150.0, Category.VEGETABLE, null, null, null, 0.0));

        List<Recipe> result = recipeRepository.findAvailableRecipes();

        assertThat(result).extracting(Recipe::getName).containsExactly("Salad");
        assertThat(result.getFirst().getIngredientsPerPerson()).hasSize(1);
    }
}

