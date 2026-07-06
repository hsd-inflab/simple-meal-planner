package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.PantryItem;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {

    List<PantryItem> findByExpirationDateBefore(LocalDate date);
}
