package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.response.EnumOptionDto;
import hsd.inflab.smp.dto.response.MetadataResponseDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Provides the enum-to-label metadata the frontend uses to render raw enum
 * values (units, categories) as localized strings.
 *
 * <p>The labels are taken from the existing {@code *_de.properties} bundles via
 * the enums' own localization maps. Locale is currently fixed to German.
 */
@Service
public class MetadataService {

    /**
     * Builds the localized options for all units and categories.
     *
     * @return the units and categories, each preserving enum declaration order
     */
    public MetadataResponseDto getMetadata() {
        return new MetadataResponseDto(getUnits(), getCategories());
    }

    /**
     * Builds the localized options for all units.
     *
     * @return the units, preserving enum declaration order
     */
    public List<EnumOptionDto> getUnits() {
        return toOptions(Unit.getLocalizedMap(Locale.GERMAN));
    }

    /**
     * Builds the localized options for all categories.
     *
     * @return the categories, preserving enum declaration order
     */
    public List<EnumOptionDto> getCategories() {
        return toOptions(Category.getLocalizedMap(Locale.GERMAN));
    }

    private static <E extends Enum<E>> List<EnumOptionDto> toOptions(Map<E, String> localizedMap) {
        return localizedMap.entrySet().stream()
                .map(entry -> new EnumOptionDto(entry.getKey().name(), entry.getValue()))
                .toList();
    }
}
