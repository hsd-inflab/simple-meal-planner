package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.mapper.PantryItemMapper;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PantryService {

    private final PantryItemRepository pantryRepo;
    private final PantryItemMapper pantryItemMapper;
    private final UserRepository userRepository;

    public PantryService(
            PantryItemRepository pantryRepo, PantryItemMapper pantryItemMapper, UserRepository userRepository) {
        this.pantryRepo = pantryRepo;
        this.pantryItemMapper = pantryItemMapper;
        this.userRepository = userRepository;
    }

    public List<PantryItemResponseDto> getPantry(String username) {
        return pantryItemMapper.toDtoList(pantryRepo.findByOwner(requireUser(username)));
    }

    public PantryItemResponseDto addItem(PantryItemRequestDto dto, String username) {
        User owner = requireUser(username);
        PantryItem item = pantryItemMapper.toEntity(dto);
        item.setOwner(owner);
        item.setGlobal(false);
        PantryItem savedEntity = pantryRepo.save(item);
        return pantryItemMapper.toDto(savedEntity);
    }

    public Optional<PantryItemResponseDto> getItemById(UUID id, String username) {
        return pantryRepo.findByIdAndOwner(id, requireUser(username)).map(pantryItemMapper::toDto);
    }

    public void deleteItem(UUID id) {
        pantryRepo.deleteById(id);
    }

    public void deleteExpiredItems(String username) {
        pantryRepo.deleteAll(pantryRepo.findByOwnerAndExpirationDateBefore(requireUser(username), LocalDate.now()));
    }

    private User requireUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }
}
