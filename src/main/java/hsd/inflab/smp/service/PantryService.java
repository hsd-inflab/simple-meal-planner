package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.repository.PantryItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PantryService {

    private final PantryItemRepository pantryRepo;

    public PantryService(PantryItemRepository pantryRepo) {
        this.pantryRepo = pantryRepo;
    }

    public List<PantryItemDto> getPantry() {
        return pantryRepo.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PantryItemDto addItem(PantryItemDto dto) {
        PantryItem entity = convertToEntity(dto);
        PantryItem savedEntity = pantryRepo.save(entity);
        return convertToDto(savedEntity);
    }

    public Optional<PantryItemDto> getItemById(UUID id) {
        return pantryRepo.findById(id).map(this::convertToDto);
    }

    public void deleteItem(UUID id) {
        pantryRepo.deleteById(id);
    }

    private PantryItemDto convertToDto(PantryItem entity) {
        return new PantryItemDto(
                entity.getId(),
                entity.getName(),
                entity.getUnit(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getExpirationDate(),
                entity.getPurchaseDate(),
                entity.getBrand(),
                entity.getPrice());
    }

    public PantryItem convertToEntity(PantryItemDto dto) {
        PantryItem entity = new PantryItem(
                dto.name(),
                dto.unit(),
                dto.amount(),
                dto.category(),
                dto.expirationDate(),
                dto.purchaseDate(),
                dto.brand(),
                dto.price());
        return entity;
    }
}
