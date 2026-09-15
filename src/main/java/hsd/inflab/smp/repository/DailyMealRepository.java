package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMealRepository extends JpaRepository<DailyMeal, UUID> {
    Optional<DailyMeal> findByOwnerUsernameAndMealDate(String username, LocalDate mealDate);

    List<DailyMeal> findByOwnerUsername(String username);

    Optional<DailyMeal> findFirstByOrderByMealDateAsc();

    // Owner scoped finders: meal plans are personal, and two users may plan the same date independently.
    List<DailyMeal> findByOwner(User owner);

    Optional<DailyMeal> findByOwnerAndMealDate(User owner, LocalDate mealDate);

    List<DailyMeal> findByOwnerAndMealDateBetween(User owner, LocalDate startDate, LocalDate endDate);

    Optional<DailyMeal> findFirstByOwnerOrderByMealDateAsc(User owner);
}
