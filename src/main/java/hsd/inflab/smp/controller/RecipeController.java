package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.RecipeDto;
import hsd.inflab.smp.service.RecipeService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public List<RecipeDto> getRecipes() {
        return recipeService.getRecipeBook();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipe(@PathVariable UUID id) {
        return recipeService.getRecipeById(id).map(ResponseEntity::ok).orElseGet(ResponseEntity.notFound()::build);
    }

    @GetMapping("/available")
    public List<RecipeDto> getAvailableRecipes() {
        return recipeService.getAvailableRecipes();
    }

    @PostMapping
    public ResponseEntity<RecipeDto> addRecipe(@RequestBody RecipeDto recipeDto) {
        RecipeDto savedRecipe = recipeService.addRecipe(recipeDto);
        return ResponseEntity.created(URI.create("/api/recipes/" + savedRecipe.id()))
                .body(savedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID id) {
        if (recipeService.getRecipeById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
