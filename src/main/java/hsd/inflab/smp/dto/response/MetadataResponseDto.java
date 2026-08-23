package hsd.inflab.smp.dto.response;

import java.util.List;

/**
 * Bundles the localized enum options the frontend needs to render raw enum
 * values as human-readable labels.
 *
 * @param units the available units with their localized labels
 * @param categories the available categories with their localized labels
 */
public record MetadataResponseDto(List<EnumOptionDto> units, List<EnumOptionDto> categories) {}
