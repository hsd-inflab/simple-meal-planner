package hsd.inflab.smp.mapper;

import hsd.inflab.smp.dto.response.DailyMealResponseDto;
import hsd.inflab.smp.entity.DailyMeal;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapping von {@link DailyMeal} zu {@link DailyMealResponseDto}.
 *
 * <p>Nutzt {@link RecipeMapper} für die verschachtelten Rezepte. Das Mapping Request -&gt; Entity (Auflösung der
 * Rezept-IDs, Upsert über das Datum) ist Geschäftslogik und verbleibt im {@code DailyMealService}.
 */
@Mapper(
        componentModel = "spring",
        uses = RecipeMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DailyMealMapper {

    @Mapping(target = "date", source = "mealDate")
    DailyMealResponseDto toDto(DailyMeal entity);

    List<DailyMealResponseDto> toDtoList(List<DailyMeal> entities);
}
