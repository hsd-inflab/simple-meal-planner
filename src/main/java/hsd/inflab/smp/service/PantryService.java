package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.mapper.PantryItemMapper;
import hsd.inflab.smp.repository.PantryItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PantryService {

    private final PantryItemRepository pantryRepo;
    private final PantryItemMapper pantryItemMapper;

    public PantryService(PantryItemRepository pantryRepo, PantryItemMapper pantryItemMapper) {
        this.pantryRepo = pantryRepo;
        this.pantryItemMapper = pantryItemMapper;
    }

    public List<PantryItemResponseDto> getPantry() {
        return pantryItemMapper.toDtoList(pantryRepo.findAll());
    }

    public PantryItemResponseDto addItem(PantryItemRequestDto dto) {
        PantryItem savedEntity = pantryRepo.save(pantryItemMapper.toEntity(dto));
        return pantryItemMapper.toDto(savedEntity);
    }

    public Optional<PantryItemResponseDto> getItemById(UUID id) {
        return pantryRepo.findById(id).map(pantryItemMapper::toDto);
    }

    public void deleteItem(UUID id) {
        pantryRepo.deleteById(id);
    }

    public void deleteExpiredItems() {
        pantryRepo.deleteAll(pantryRepo.findByExpirationDateBefore(LocalDate.now()));
    }
}
