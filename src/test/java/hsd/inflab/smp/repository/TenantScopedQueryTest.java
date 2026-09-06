package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the owner scoped finders really hide the data of other users. These queries are the only barrier
 * between two tenants, so they are tested against the database instead of a mock.
 */
@SpringBootTest(classes = TenantScopedQueryTest.RepositoryTestApplication.class)
@ActiveProfiles("test")
class TenantScopedQueryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private DailyMealRepository dailyMealRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        dailyMealRepository.deleteAll();
        pantryItemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User("scope-owner", "test-password-hash", List.of(Role.USER)));
        otherUser = userRepository.save(new User("scope-other", "test-password-hash", List.of(Role.USER)));
    }

    @Test
    void findByOwner_returnsOnlyOwnPantryItems() {
        // Arrange
        pantryItemRepository.save(pantryItem("Own item", owner, null));
        pantryItemRepository.save(pantryItem("Foreign item", otherUser, null));

        // Act
        List<PantryItem> result = pantryItemRepository.findByOwner(owner);

        // Assert
        assertThat(result).extracting(PantryItem::getName).containsExactly("Own item");
    }

    @Test
    void findByIdAndOwner_hidesForeignPantryItems() {
        // Arrange
        PantryItem foreignItem = pantryItemRepository.save(pantryItem("Foreign item", otherUser, null));

        // Act and Assert
        assertThat(pantryItemRepository.findByIdAndOwner(foreignItem.getId(), owner))
                .isEmpty();
        assertThat(pantryItemRepository.findByIdAndOwner(foreignItem.getId(), otherUser))
                .isPresent();
    }

    @Test
    void findByOwnerAndExpirationDateBefore_ignoresForeignExpiredItems() {
        // Arrange
        LocalDate expired = LocalDate.now().minusDays(1);
        pantryItemRepository.save(pantryItem("Own expired", owner, expired));
        pantryItemRepository.save(pantryItem("Foreign expired", otherUser, expired));
        pantryItemRepository.save(pantryItem("Own fresh", owner, LocalDate.now().plusDays(10)));

        // Act
        List<PantryItem> result = pantryItemRepository.findByOwnerAndExpirationDateBefore(owner, LocalDate.now());

        // Assert
        assertThat(result).extracting(PantryItem::getName).containsExactly("Own expired");
    }

    @Test
    void findByOwnerAndMealDate_separatesPlansOfDifferentUsersOnTheSameDay() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 7, 11);
        dailyMealRepository.save(dailyMeal(date, owner, 1));
        dailyMealRepository.save(dailyMeal(date, otherUser, 2));

        // Act and Assert
        assertThat(dailyMealRepository.findByOwnerAndMealDate(owner, date))
                .get()
                .extracting(DailyMeal::getBreakfastServings)
                .isEqualTo(1);
        assertThat(dailyMealRepository.findByOwnerAndMealDate(otherUser, date))
                .get()
                .extracting(DailyMeal::getBreakfastServings)
                .isEqualTo(2);
    }

    @Test
    void findByOwnerAndMealDateBetween_returnsOnlyOwnPlans() {
        // Arrange
        dailyMealRepository.save(dailyMeal(LocalDate.of(2026, 7, 11), owner, 1));
        dailyMealRepository.save(dailyMeal(LocalDate.of(2026, 7, 12), otherUser, 2));

        // Act
        List<DailyMeal> result = dailyMealRepository.findByOwnerAndMealDateBetween(
                owner, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        // Assert
        assertThat(result).extracting(DailyMeal::getMealDate).containsExactly(LocalDate.of(2026, 7, 11));
    }

    @Test
    void findFirstByOwnerOrderByMealDateAsc_ignoresEarlierForeignPlans() {
        // Arrange
        dailyMealRepository.save(dailyMeal(LocalDate.of(2026, 7, 1), otherUser, 2));
        dailyMealRepository.save(dailyMeal(LocalDate.of(2026, 7, 20), owner, 1));

        // Act and Assert
        assertThat(dailyMealRepository.findFirstByOwnerOrderByMealDateAsc(owner))
                .get()
                .extracting(DailyMeal::getMealDate)
                .isEqualTo(LocalDate.of(2026, 7, 20));
    }

    private PantryItem pantryItem(String name, User itemOwner, LocalDate expirationDate) {
        PantryItem item = new PantryItem(name, Unit.UNIT, 1.0, Category.NONE, expirationDate, null, null, 0.0);
        item.setOwner(itemOwner);
        item.setGlobal(false);
        return item;
    }

    private DailyMeal dailyMeal(LocalDate date, User mealOwner, int breakfastServings) {
        DailyMeal meal = new DailyMeal();
        meal.setMealDate(date);
        meal.setOwner(mealOwner);
        meal.setGlobal(false);
        meal.setBreakfastServings(breakfastServings);
        return meal;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("hsd.inflab.smp.entity")
    @EnableJpaRepositories("hsd.inflab.smp.repository")
    static class RepositoryTestApplication {}
}
