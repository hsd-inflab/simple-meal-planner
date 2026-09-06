package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.service.RecipeService;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeResponseDto> getRecipes() {
        return recipeService.getRecipeBook();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getRecipe(@PathVariable UUID id) {
        return recipeService.getRecipeById(id).map(ResponseEntity::ok).orElseGet(ResponseEntity.notFound()::build);
    }

    @GetMapping("/available")
    public List<RecipeResponseDto> getAvailableRecipes() {
        return recipeService.getAvailableRecipes();
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDto> addRecipe(
            @RequestBody RecipeRequestDto recipeRequest, Principal principal) {
        RecipeResponseDto savedRecipe = recipeService.addRecipe(recipeRequest, principal.getName());
        return ResponseEntity.created(URI.create("/api/recipes/" + savedRecipe.id()))
                .body(savedRecipe);
    }
}
