package hsd.inflab.smp.dto.external;

import java.util.List;

public record ExternalRecipeDto(
        String title, String source, String keywords, List<ExternalRecipeIngredientDto> ingredients) {}
