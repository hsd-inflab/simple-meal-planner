package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.mapper.RecipeMapper;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepo;
    private final RecipeMapper recipeMapper;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepo, RecipeMapper recipeMapper, UserRepository userRepository) {
        this.recipeRepo = recipeRepo;
        this.recipeMapper = recipeMapper;
        this.userRepository = userRepository;
    }

    public List<RecipeResponseDto> getRecipeBook(String username) {
        return recipeMapper.toDtoList(recipeRepo.findVisibleToUser(username));
    }

    public RecipeResponseDto addRecipe(RecipeRequestDto dto, String username) {
        User owner = requireUser(username);
        Recipe recipe = recipeMapper.toEntity(dto);
        recipe.setOwner(owner);
        recipe.setGlobal(false);
        Recipe savedEntity = recipeRepo.save(recipe);
        return recipeMapper.toDto(savedEntity);
    }

    public Optional<RecipeResponseDto> getRecipeById(UUID id, String username) {
        return recipeRepo.findVisibleById(id, username).map(recipeMapper::toDto);
    }

    public List<RecipeResponseDto> getAvailableRecipes(String username) {
        return recipeMapper.toDtoList(recipeRepo.findAvailableRecipes(username));
    }

    private User requireUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }
}
