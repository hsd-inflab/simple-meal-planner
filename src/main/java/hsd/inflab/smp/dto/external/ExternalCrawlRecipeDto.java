package hsd.inflab.smp.dto.external;

import java.util.List;

public record ExternalCrawlRecipeDto(
        String title, String source, List<ExternalRecipeIngredientDto> ingredients, List<String> steps) {}
