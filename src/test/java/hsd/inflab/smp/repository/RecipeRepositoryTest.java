package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RecipeRepositoryTest.RepositoryTestApplication.class)
@ActiveProfiles("test")
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        pantryItemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User("recipe-owner", "test-password-hash", List.of(Role.USER)));
        otherUser = userRepository.save(new User("other-user", "test-password-hash", List.of(Role.USER)));
    }

    @Test
    void findAvailableRecipes_returnsOnlyRecipesWithEnoughPantryItems() {
        // Arrange
        RecipeIngredient tomato = new RecipeIngredient("TOMATO", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        RecipeIngredient cheese = new RecipeIngredient("Cheese", Unit.G, 50.0, Category.DAIRY, "dairy", "grated");
        Recipe availableRecipe = new Recipe("Salad", "Can be cooked", List.of(tomato));
        Recipe unavailableRecipe = new Recipe("Pizza", "Cannot be cooked", List.of(cheese));

        recipeRepository.saveAll(List.of(availableRecipe, unavailableRecipe));
        pantryItemRepository.save(new PantryItem("tomato", Unit.G, 150.0, Category.VEGETABLE, null, null, null, 0.0));

        // Act
        List<Recipe> result = recipeRepository.findAvailableRecipes();

        // Assert
        assertThat(result).extracting(Recipe::getName).containsExactly("Salad");
        assertThat(result.get(0).getIngredientsPerPerson()).hasSize(1);
    }

    @Test
    void findByGlobalTrue_returnsOnlyStandardRecipes() {
        // Arrange
        recipeRepository.save(global(new Recipe("Standard", "shared", List.of())));
        recipeRepository.save(ownedBy(new Recipe("Private", "personal", List.of()), owner));

        // Act
        List<Recipe> result = recipeRepository.findByGlobalTrue();

        // Assert
        assertThat(result).extracting(Recipe::getName).containsExactly("Standard");
    }

    @Test
    void findVisibleFor_returnsGlobalRecipesAndOwnRecipesOnly() {
        // Arrange
        recipeRepository.save(global(new Recipe("Standard", "shared", List.of())));
        recipeRepository.save(ownedBy(new Recipe("Own", "personal", List.of()), owner));
        recipeRepository.save(ownedBy(new Recipe("Foreign", "someone else", List.of()), otherUser));

        // Act
        List<Recipe> result = recipeRepository.findVisibleFor(owner);

        // Assert
        assertThat(result).extracting(Recipe::getName).containsExactlyInAnyOrder("Standard", "Own");
    }

    @Test
    void findVisibleById_hidesForeignPrivateRecipes() {
        // Arrange
        Recipe foreignRecipe = recipeRepository.save(ownedBy(new Recipe("Foreign", "x", List.of()), otherUser));
        Recipe globalRecipe = recipeRepository.save(global(new Recipe("Standard", "y", List.of())));

        // Act
        Optional<Recipe> foreignResult = recipeRepository.findVisibleById(foreignRecipe.getId(), owner);
        Optional<Recipe> globalResult = recipeRepository.findVisibleById(globalRecipe.getId(), owner);

        // Assert
        assertThat(foreignResult).isEmpty();
        assertThat(globalResult).isPresent();
    }

    @Test
    void findAvailableRecipesFor_ignoresPantryItemsOfOtherUsers() {
        // Arrange: only the other user stocks the ingredient
        RecipeIngredient tomato = new RecipeIngredient("Tomato", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        recipeRepository.save(global(new Recipe("Salad", "needs tomato", List.of(tomato))));
        pantryItemRepository.save(
                ownedBy(new PantryItem("tomato", Unit.G, 150.0, Category.VEGETABLE, null, null, null, 0.0), otherUser));

        // Act
        List<Recipe> result = recipeRepository.findAvailableRecipesFor(owner);

        // Assert: a foreign pantry must never make a recipe available
        assertThat(result).isEmpty();
    }

    @Test
    void findAvailableRecipesFor_usesTheOwnPantry() {
        // Arrange
        RecipeIngredient tomato = new RecipeIngredient("Tomato", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        recipeRepository.save(global(new Recipe("Salad", "needs tomato", List.of(tomato))));
        pantryItemRepository.save(
                ownedBy(new PantryItem("tomato", Unit.G, 150.0, Category.VEGETABLE, null, null, null, 0.0), owner));

        // Act
        List<Recipe> result = recipeRepository.findAvailableRecipesFor(owner);

        // Assert
        assertThat(result).extracting(Recipe::getName).containsExactly("Salad");
    }

    @Test
    void findAvailableRecipesFor_hidesForeignPrivateRecipes() {
        // Arrange: the other user owns a recipe whose ingredients this user happens to have
        RecipeIngredient tomato = new RecipeIngredient("Tomato", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        recipeRepository.save(ownedBy(new Recipe("Foreign salad", "private", List.of(tomato)), otherUser));
        pantryItemRepository.save(
                ownedBy(new PantryItem("tomato", Unit.G, 150.0, Category.VEGETABLE, null, null, null, 0.0), owner));

        // Act
        List<Recipe> result = recipeRepository.findAvailableRecipesFor(owner);

        // Assert
        assertThat(result).isEmpty();
    }

    private Recipe global(Recipe recipe) {
        recipe.setGlobal(true);
        return recipe;
    }

    private Recipe ownedBy(Recipe recipe, User recipeOwner) {
        recipe.setOwner(recipeOwner);
        recipe.setGlobal(false);
        return recipe;
    }

    private PantryItem ownedBy(PantryItem item, User itemOwner) {
        item.setOwner(itemOwner);
        item.setGlobal(false);
        return item;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("hsd.inflab.smp.entity")
    @EnableJpaRepositories("hsd.inflab.smp.repository")
    static class RepositoryTestApplication {}
}
