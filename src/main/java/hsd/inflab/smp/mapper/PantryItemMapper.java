package hsd.inflab.smp.mapper;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.entity.PantryItem;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Mapping zwischen {@link PantryItem} und {@link PantryItemResponseDto}.
 *
 * <p>Die {@code id} besitzt keinen Setter und wird beim Mapping DTO -&gt; Entity nicht gesetzt.
 */
@Mapper(componentModel = "spring")
public interface PantryItemMapper {

    PantryItemResponseDto toDto(PantryItem entity);

    List<PantryItemResponseDto> toDtoList(List<PantryItem> entities);

    PantryItem toEntity(PantryItemRequestDto dto);
}
