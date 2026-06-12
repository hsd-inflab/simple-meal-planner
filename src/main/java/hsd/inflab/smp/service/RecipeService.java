package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.dto.RecipeIngredientDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepo;

    public RecipeService(RecipeRepository recipeRepo) {
        this.recipeRepo = recipeRepo;
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
                .map(this::convertIngredientToDto)
                .toList();
        return new RecipeDto(entity.getId(), entity.getName(), entity.getDescription(), ingredientDtos);
    }

    private RecipeIngredientDto convertIngredientToDto(RecipeIngredient entity) {
        return new RecipeIngredientDto(
                entity.getId(),
                entity.getName(),
                entity.getUnit(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getFoodType(),
                entity.getPreparation());
    }

    // Wrapper: Private -> Public convertIngredientToDto, damit RecipeAPIService darauf zugreifen kann, ohne die ganze
    // RecipeService-Logik zu verändern
    public RecipeIngredientDto ingredientToDto(RecipeIngredient entity) {
        return convertIngredientToDto(entity);
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
        return recipeRepo.findAvailableRecipes().stream()
                .map(this::convertToDto)
                .toList();
    }
}
