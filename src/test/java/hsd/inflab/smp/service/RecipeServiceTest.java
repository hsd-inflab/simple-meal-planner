package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.request.RecipeIngredientRequestDto;
import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.mapper.RecipeMapper;
import hsd.inflab.smp.mapper.RecipeMapperImpl;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
// Unit-Tests fuer RecipeService: Logik wird isoliert mit Mockito gegenueber den Repositories getestet.
// Das Mapping selbst wird vom echten RecipeMapper uebernommen (siehe RecipeMapperTest).
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepo;

    @Mock
    private UserRepository userRepository;

    @Spy
    private RecipeMapper recipeMapper = new RecipeMapperImpl();

    @InjectMocks
    private RecipeService recipeService;

    /**
     * Prueft, ob das Rezeptbuch aus dem Repository gelesen und in DTOs gemappt wird.
     */
    @Test
    void getRecipeBook_returnsMappedDtos() {
        // Arrange: Zwei Rezepte im Repository simulieren.
        String username = "recipe-owner";
        User owner = new User(username, "test-password-hash", List.of(Role.USER));
        Recipe first = new Recipe("Soup", "Warm soup", List.of());
        Recipe second = new Recipe("Salad", "Fresh salad", List.of());
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(recipeRepo.findVisibleFor(owner)).thenReturn(List.of(first, second));

        // Act: Rezeptbuch laden.
        List<RecipeResponseDto> result = recipeService.getRecipeBook(username);

        // Assert: Beide Rezepte muessen vorhanden und in der richtigen Reihenfolge sein.
        assertEquals(2, result.size());
        assertEquals("Soup", result.get(0).name());
        assertEquals("Salad", result.get(1).name());

        // Interaktionspruefung: globale und eigene Rezepte, niemals das ungefilterte findAll.
        verify(recipeRepo).findVisibleFor(owner);
        verify(recipeRepo, never()).findAll();
    }

    /**
     * Prueft das Speichern eines neuen Rezepts.
     * Erwartung: Das DTO wird in ein Entity umgewandelt, gespeichert und als DTO zurueckgegeben.
     */
    @Test
    void addRecipe_returnsSavedDto() {
        // Arrange: Eine Rezeptzutat und das Eingabe-Request-DTO vorbereiten.
        String username = "recipe-owner";
        User owner = new User(username, "test-password-hash", List.of(Role.USER));
        RecipeIngredientRequestDto ingredientDto =
                new RecipeIngredientRequestDto("Milk", Unit.L, 1.0, Category.DAIRY, "dairy", "fresh");
        RecipeRequestDto input = new RecipeRequestDto("Porridge", "Breakfast", List.of(ingredientDto));

        // Das Save-Verhalten wird so simuliert, dass das uebergebene Entity direkt zurueckkommt.
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(recipeRepo.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Rezept ueber den Service speichern.
        RecipeResponseDto result = recipeService.addRecipe(input, username);

        // Assert: Rueckgabe muss den Eingabedaten entsprechen.
        assertEquals("Porridge", result.name());
        assertEquals("Breakfast", result.description());
        assertEquals(1, result.ingredientsPerPerson().size());
        assertEquals("Milk", result.ingredientsPerPerson().getFirst().name());

        ArgumentCaptor<Recipe> savedRecipe = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepo).save(savedRecipe.capture());
        assertSame(owner, savedRecipe.getValue().getOwner());
        assertFalse(savedRecipe.getValue().isGlobal());
        verify(userRepository).findByUsername(username);
    }

    /**
     * Prueft, ob verfuegbare Rezepte aus der DB-Query gelesen und gemappt werden.
     */
    @Test
    void getAvailableRecipes_returnsRepositoryResultAsDtos() {
        // Arrange: Die Repository-Query liefert bereits nur kochbare Rezepte.
        RecipeIngredient availableIngredient =
                new RecipeIngredient("TOMATO", Unit.G, 100.0, Category.VEGETABLE, "veg", "cut");
        Recipe availableRecipe = new Recipe("Salad", "Can be cooked", List.of(availableIngredient));
        String username = "recipe-owner";
        User owner = new User(username, "test-password-hash", List.of(Role.USER));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(recipeRepo.findAvailableRecipesFor(owner)).thenReturn(List.of(availableRecipe));

        // Act: Verfuegbare Rezepte ermitteln.
        List<RecipeResponseDto> result = recipeService.getAvailableRecipes(username);

        // Assert: Nur das kochbare Rezept darf im Ergebnis stehen.
        assertEquals(1, result.size());
        assertEquals("Salad", result.getFirst().name());

        // Das Matching passiert im Repository, nicht im Service - und immer eigentuemerbezogen.
        verify(recipeRepo).findAvailableRecipesFor(owner);
        verify(recipeRepo, never()).findAvailableRecipes();
    }

    /**
     * Prueft die Mandantentrennung beim Einzelzugriff:
     * Ein privates Rezept eines anderen Users darf ueber seine ID nicht lesbar sein.
     */
    @Test
    void getRecipeById_whenRecipeIsNotVisibleForUser_returnsEmpty() {
        // Arrange
        String username = "recipe-owner";
        User owner = new User(username, "test-password-hash", List.of(Role.USER));
        UUID foreignRecipeId = UUID.randomUUID();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(recipeRepo.findVisibleById(foreignRecipeId, owner)).thenReturn(Optional.empty());

        // Act
        Optional<RecipeResponseDto> result = recipeService.getRecipeById(foreignRecipeId, username);

        // Assert
        assertTrue(result.isEmpty());
        verify(recipeRepo, never()).findById(foreignRecipeId);
    }
}
