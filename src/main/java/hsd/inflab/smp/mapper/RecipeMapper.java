package hsd.inflab.smp.mapper;

import hsd.inflab.smp.dto.request.RecipeIngredientRequestDto;
import hsd.inflab.smp.dto.request.RecipeRequestDto;
import hsd.inflab.smp.dto.response.RecipeIngredientResponseDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Mapping zwischen {@link Recipe}/{@link RecipeIngredient} und ihren DTOs.
 *
 * <p>Die {@code id} der Entities besitzt keinen Setter und wird daher beim Mapping DTO -&gt; Entity nicht gesetzt
 * (sie wird von JPA per {@code @GeneratedValue} vergeben).
 */
@Mapper(componentModel = "spring")
public interface RecipeMapper {

    RecipeResponseDto toDto(Recipe entity);

    RecipeIngredientResponseDto toDto(RecipeIngredient entity);

    List<RecipeResponseDto> toDtoList(List<Recipe> entities);

    Recipe toEntity(RecipeRequestDto dto);

    RecipeIngredient toEntity(RecipeIngredientRequestDto dto);
}
