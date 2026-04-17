package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.DailyMeal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMealRepository extends JpaRepository<DailyMeal, UUID> {
    Optional<DailyMeal> findByMealDate(LocalDate mealDate);

    List<DailyMeal> findByMealDateBetween(LocalDate startDate, LocalDate endDate);
}
