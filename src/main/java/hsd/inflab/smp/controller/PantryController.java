package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.service.PantryService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {
    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GetMapping
    public List<PantryItemDto> getPantry() {
        return pantryService.getPantry();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PantryItemDto> getPantryItem(@PathVariable UUID id) {
        return pantryService.getItemById(id).map(ResponseEntity::ok).orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping
    public ResponseEntity<PantryItemDto> addItem(@RequestBody PantryItemDto pantryItemDto) {
        PantryItemDto savedItem = pantryService.addItem(pantryItemDto);
        return ResponseEntity.created(URI.create("/api/pantry/" + savedItem.id()))
                .body(savedItem);
    }
}
