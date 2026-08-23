package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.response.EnumOptionDto;
import hsd.inflab.smp.dto.response.MetadataResponseDto;
import hsd.inflab.smp.service.MetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the enum-to-label metadata (units, categories) the frontend needs to
 * render raw enum values as localized strings.
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping
    public MetadataResponseDto getMetadata() {
        return metadataService.getMetadata();
    }

    @GetMapping("/units")
    public List<EnumOptionDto> getUnits() {
        return metadataService.getUnits();
    }

    @GetMapping("/categories")
    public List<EnumOptionDto> getCategories() {
        return metadataService.getCategories();
    }
}
