package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.DailyMeal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMealRepository extends JpaRepository<DailyMeal, UUID> {
    Optional<DailyMeal> findByOwnerUsernameAndMealDate(String username, LocalDate mealDate);

    List<DailyMeal> findByOwnerUsername(String username);

    List<DailyMeal> findByOwnerUsernameAndMealDateBetween(String username, LocalDate startDate, LocalDate endDate);

    Optional<DailyMeal> findFirstByOwnerUsernameOrderByMealDateAsc(String username);
}
