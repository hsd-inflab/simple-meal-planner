package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.PantryItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    @Query(
            """
            SELECT p
            FROM PantryItem p
            LEFT JOIN p.owner owner
            WHERE p.global = true OR owner.username = :username
            """)
    List<PantryItem> findVisibleToUser(@Param("username") String username);

    @Query(
            """
            SELECT p
            FROM PantryItem p
            LEFT JOIN p.owner owner
            WHERE p.id = :id
            AND (p.global = true OR owner.username = :username)
            """)
    Optional<PantryItem> findVisibleById(@Param("id") UUID id, @Param("username") String username);

    List<PantryItem> findByExpirationDateBefore(LocalDate date);
}
