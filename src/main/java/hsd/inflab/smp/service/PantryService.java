package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.repository.PantryItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PantryService {

    private final PantryItemRepository pantryRepo;

    public PantryService(PantryItemRepository pantryRepo) {
        this.pantryRepo = pantryRepo;
    }

    private PantryItemDto convertToDto(PantryItem entity) {
        return new PantryItemDto(
                entity.getID(),
                entity.getName(),
                entity.getUnit(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getExpirationDate(),
                entity.getPurchaseDate(),
                entity.getBrand(),
                entity.getPrice());
    }

    public List<PantryItemDto> getPantry() {
        return pantryRepo.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PantryItemDto addItem(PantryItemDto dto) {
        PantryItem entity = new PantryItem(
                dto.name(),
                dto.unit(),
                dto.amount(),
                dto.category(),
                dto.expirationDate(),
                dto.purchaseDate(),
                dto.brand(),
                dto.price());
        PantryItem savedEntity = pantryRepo.save(entity);
        return convertToDto(savedEntity);
    }
}
