package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.dto.RecipeIngredientDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepo;
    private final PantryItemRepository pantryRepo;

    public RecipeService(RecipeRepository recipeRepo, PantryItemRepository pantryRepo) {
        this.recipeRepo = recipeRepo;
        this.pantryRepo = pantryRepo;
    }

    public List<RecipeDto> getRecipeBook() {
        return recipeRepo.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public RecipeDto addRecipe(RecipeDto dto) {

        Recipe entity = convertToEntity(dto);
        Recipe savedEntity = recipeRepo.save(entity);

        return convertToDto(savedEntity);
    }

    public Optional<RecipeDto> getRecipeById(UUID id) {
        return recipeRepo.findById(id).map(this::convertToDto);
    }

    public void deleteRecipe(UUID id) {
        recipeRepo.deleteById(id);
    }

    public RecipeDto convertToDto(Recipe entity) {
        List<RecipeIngredientDto> ingredientDtos = entity.getIngredientsPerPerson().stream()
                .map(ingredient -> new RecipeIngredientDto(
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getUnit(),
                        ingredient.getAmount(),
                        ingredient.getCategory(),
                        ingredient.getFoodType(),
                        ingredient.getPreparation()))
                .toList();
        return new RecipeDto(entity.getId(), entity.getName(), entity.getDescription(), ingredientDtos);
    }

    private Recipe convertToEntity(RecipeDto dto) {
        Recipe entity = new Recipe();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        if (dto.ingredientsPerPerson() != null) {
            for (RecipeIngredientDto ingrendientDto : dto.ingredientsPerPerson()) {
                RecipeIngredient ingredient = new RecipeIngredient(
                        ingrendientDto.name(),
                        ingrendientDto.unit(),
                        ingrendientDto.amount(),
                        ingrendientDto.category(),
                        ingrendientDto.foodType(),
                        ingrendientDto.preparation());
                entity.addIngredient(ingredient);
            }
        }
        return entity;
    }

    // todo: remove when not needed anymore
    public RecipeDto convertToDtoTempWrapper(Recipe entity) {
        return convertToDto(entity);
    }

    // todo: remove when not needed anymore
    public Recipe convertToEntityTempWrapper(RecipeDto dto) {
        return convertToEntity(dto);
    }

    public List<RecipeDto> getAvailableRecipes() {
        List<RecipeDto> availableRecipes = new ArrayList<>();
        List<Recipe> recipeBook = recipeRepo.findAll();
        List<PantryItem> pantry = pantryRepo.findAll();

        for (Recipe recipe : recipeBook) {
            boolean canMake = true;

            // Durchlaufe alle Zutaten des Rezepts
            for (RecipeIngredient recipeIng : recipe.getIngredientsPerPerson()) {
                // Suche nach der Zutat in der Pantry
                Optional<PantryItem> matchingItem = pantry.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(recipeIng.getName())) // Vergleiche die Namen der
                        // Zutaten
                        .findFirst(); // Finde das erste PantryItem, das der Zutat entspricht

                // Prüfe, ob die Zutat in der Pantry vorhanden ist und ob die Menge ausreicht
                if (matchingItem.isEmpty() || matchingItem.get().getAmount() < recipeIng.getAmount()) {
                    canMake = false; // Rezept kann nicht gemacht werden, da Zutat fehlt oder Menge nicht ausreicht
                    break; // Schleife abbrechen, da es nicht mehr möglich ist, das Rezept zu machen
                }
            }

            // Wenn das Rezept mit den Zutaten zubereitet werden kann, füge es zur Liste der
            // verfügbaren Rezepte hinzu
            if (canMake) {
                RecipeDto dto = convertToDto(recipe);
                availableRecipes.add(dto);
            }
        }

        return availableRecipes;
    }
}
