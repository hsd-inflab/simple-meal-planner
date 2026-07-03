package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.mapper.RecipeMapper;
import hsd.inflab.smp.repository.RecipeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepo;
    private final RecipeMapper recipeMapper;

    public RecipeService(RecipeRepository recipeRepo, RecipeMapper recipeMapper) {
        this.recipeRepo = recipeRepo;
        this.recipeMapper = recipeMapper;
    }

    public List<RecipeResponseDto> getRecipeBook() {
        return recipeMapper.toDtoList(recipeRepo.findAll());
    }

    public RecipeResponseDto addRecipe(RecipeRequestDto dto) {
        Recipe savedEntity = recipeRepo.save(recipeMapper.toEntity(dto));
        return recipeMapper.toDto(savedEntity);
    }

    public Optional<RecipeResponseDto> getRecipeById(UUID id) {
        return recipeRepo.findById(id).map(recipeMapper::toDto);
    }

    public void deleteRecipe(UUID id) {
        recipeRepo.deleteById(id);
    }

    public List<RecipeResponseDto> getAvailableRecipes() {
        return recipeMapper.toDtoList(recipeRepo.findAvailableRecipes());
    }
}
