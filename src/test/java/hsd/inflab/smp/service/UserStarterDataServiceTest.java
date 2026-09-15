package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Starter data is created for exactly one user. Pantry items and daily meals are personal and therefore owned;
 * the recipes they reference are the shared global standard recipes and are never copied.
 */
class UserStarterDataServiceTest {

    private PantryItemRepository pantryItemRepository;
    private DailyMealRepository dailyMealRepository;
    private RecipeRepository recipeRepository;

    private UserStarterDataService service;

    private User owner;

    @BeforeEach
    void setUp() {
        pantryItemRepository = mock(PantryItemRepository.class);
        dailyMealRepository = mock(DailyMealRepository.class);
        recipeRepository = mock(RecipeRepository.class);

        service = new UserStarterDataService(
                pantryItemRepository, dailyMealRepository, recipeRepository, List.of(Role.USER));
        owner = new User("BravePanda07", "hashed-password", List.of(Role.USER));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createStarterDataFor_savesPantryItemsOwnedByUser() {
        // Arrange
        when(recipeRepository.findByGlobalTrue()).thenReturn(List.of());
        ArgumentCaptor<List<PantryItem>> savedItems = ArgumentCaptor.forClass(List.class);

        // Act
        service.createStarterDataFor(owner);

        // Assert
        verify(pantryItemRepository).saveAll(savedItems.capture());
        assertThat(savedItems.getValue()).isNotEmpty().allSatisfy(item -> {
            assertThat(item.getOwner()).isSameAs(owner);
            assertThat(item.isGlobal()).isFalse();
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void createStarterDataFor_savesDailyMealsOwnedByUser() {
        // Arrange
        when(recipeRepository.findByGlobalTrue()).thenReturn(globalRecipes());
        ArgumentCaptor<List<DailyMeal>> savedMeals = ArgumentCaptor.forClass(List.class);

        // Act
        service.createStarterDataFor(owner);

        // Assert
        verify(dailyMealRepository).saveAll(savedMeals.capture());
        assertThat(savedMeals.getValue()).isNotEmpty().allSatisfy(meal -> {
            assertThat(meal.getOwner()).isSameAs(owner);
            assertThat(meal.isGlobal()).isFalse();
            assertThat(meal.getMealDate()).isNotNull();
        });
    }

    @Test
    void createStarterDataFor_referencesOnlyGlobalRecipes() {
        // Arrange
        List<Recipe> globalRecipes = globalRecipes();
        when(recipeRepository.findByGlobalTrue()).thenReturn(globalRecipes);

        // Act
        service.createStarterDataFor(owner);

        // Assert
        verify(recipeRepository).findByGlobalTrue();
        verify(recipeRepository, Mockito.never()).findAll();
    }

    @Test
    void createStarterDataFor_createsNoDailyMeals_whenNoGlobalRecipeExists() {
        // Arrange: the autofiller is switched off, so no standard recipes exist
        when(recipeRepository.findByGlobalTrue()).thenReturn(List.of());

        // Act
        service.createStarterDataFor(owner);

        // Assert: the pantry is still created, the registration must not fail
        verify(pantryItemRepository).saveAll(ArgumentMatchers.anyList());
        verifyNoInteractions(dailyMealRepository);
    }

    private List<Recipe> globalRecipes() {
        return List.of(
                new Recipe("Grundbasis Bolognese", "Fleischsauce", List.of()),
                new Recipe("Caffè Latte", "Kaffeegetränk", List.of()),
                new Recipe("Schnelle Apfeltarte", "Blätterteig mit Äpfeln", List.of()));
    }

    @Test
    void createStarterDataFor_createsNothing_whenUserHasNoConfiguredRole() {
        // Arrange: starter data is configured for ADMIN, the new user only has USER
        UserStarterDataService adminOnlyService = new UserStarterDataService(
                pantryItemRepository, dailyMealRepository, recipeRepository, List.of(Role.ADMIN));

        // Act
        adminOnlyService.createStarterDataFor(owner);

        // Assert: not even the recipes are looked up
        verifyNoInteractions(pantryItemRepository);
        verifyNoInteractions(dailyMealRepository);
        verifyNoInteractions(recipeRepository);
    }

    @Test
    void createStarterDataFor_createsData_whenOneOfSeveralRolesMatches() {
        // Arrange: the user carries both roles, the configuration only lists ADMIN
        User adminUser = new User("AdminUser01", "hashed-password", List.of(Role.USER, Role.ADMIN));
        UserStarterDataService adminOnlyService = new UserStarterDataService(
                pantryItemRepository, dailyMealRepository, recipeRepository, List.of(Role.ADMIN));
        when(recipeRepository.findByGlobalTrue()).thenReturn(List.of());

        // Act
        adminOnlyService.createStarterDataFor(adminUser);

        // Assert
        verify(pantryItemRepository).saveAll(ArgumentMatchers.anyList());
    }

    @Test
    void createStarterDataFor_createsNothing_whenConfiguredRoleListIsEmpty() {
        // Arrange: an empty list switches the feature off just as effectively as the enabled flag
        UserStarterDataService noRoleService =
                new UserStarterDataService(pantryItemRepository, dailyMealRepository, recipeRepository, List.of());

        // Act
        noRoleService.createStarterDataFor(owner);

        // Assert
        verifyNoInteractions(pantryItemRepository);
        verifyNoInteractions(dailyMealRepository);
        verifyNoInteractions(recipeRepository);
    }
}
