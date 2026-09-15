package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    List<PantryItem> findByExpirationDateBefore(LocalDate date);

    // Owner scoped finders: a pantry is personal, so nothing may be read across tenants.
    List<PantryItem> findByOwner(User owner);

    Optional<PantryItem> findByIdAndOwner(UUID id, User owner);

    List<PantryItem> findByOwnerAndExpirationDateBefore(User owner, LocalDate date);
}
