package hsd.inflab.smp.dto;

import hsd.inflab.smp.entity.RecipeIngredient;
import java.util.List;

public record RecipeAPIDto(String title, String description, List<RecipeIngredient> ingredients) {}
