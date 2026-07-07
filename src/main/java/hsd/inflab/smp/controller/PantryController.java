package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.service.PantryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {
    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GetMapping
    public List<PantryItemResponseDto> getPantry() {
        return pantryService.getPantry();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PantryItemResponseDto> getPantryItem(@PathVariable UUID id) {
        return pantryService.getItemById(id).map(ResponseEntity::ok).orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping
    public ResponseEntity<PantryItemResponseDto> addItem(@RequestBody PantryItemRequestDto pantryItemRequest) {
        PantryItemResponseDto savedItem = pantryService.addItem(pantryItemRequest);
        return ResponseEntity.created(URI.create("/api/pantry/" + savedItem.id()))
                .body(savedItem);
    }

    @DeleteMapping("/expired")
    public ResponseEntity<Void> deleteExpiredItems() {
        pantryService.deleteExpiredItems();
        return ResponseEntity.noContent().build(); // noContent() statt ok()
    }
}
