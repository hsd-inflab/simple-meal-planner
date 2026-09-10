package hsd.inflab.smp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TenantOwnershipRepositoryTest.RepositoryTestApplication.class)
@ActiveProfiles("test")
@Transactional
class TenantOwnershipRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private DailyMealRepository dailyMealRepository;

    @Test
    void personalRecords_storeTheirOwnerForAllTenantEntities() throws Exception {
        // Arrange
        User owner = userRepository.save(new User("record-owner", "test-password-hash", List.of(Role.USER)));
        Recipe recipe = new Recipe("Owned recipe", "Description", List.of());
        PantryItem pantryItem = new PantryItem("Owned item", Unit.UNIT, 1.0, Category.NONE, null, null, null, 0.0);
        DailyMeal dailyMeal = new DailyMeal();
        dailyMeal.setMealDate(LocalDate.of(2026, 7, 11));
        setTenantState(recipe, owner, false);
        setTenantState(pantryItem, owner, false);
        setTenantState(dailyMeal, owner, false);

        // Act
        UUID recipeId = recipeRepository.save(recipe).getId();
        UUID pantryItemId = pantryItemRepository.save(pantryItem).getId();
        UUID dailyMealId = dailyMealRepository.save(dailyMeal).getId();
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertTenantState(recipeRepository.findById(recipeId).orElseThrow(), owner.getId(), false);
        assertTenantState(pantryItemRepository.findById(pantryItemId).orElseThrow(), owner.getId(), false);
        assertTenantState(dailyMealRepository.findById(dailyMealId).orElseThrow(), owner.getId(), false);
    }

    @Test
    void globalRecords_storeWithoutAnOwnerForAllTenantEntities() throws Exception {
        // Arrange
        Recipe recipe = new Recipe("Global recipe", "Description", List.of());
        PantryItem pantryItem = new PantryItem("Global item", Unit.UNIT, 1.0, Category.NONE, null, null, null, 0.0);
        DailyMeal dailyMeal = new DailyMeal();
        dailyMeal.setMealDate(LocalDate.of(2026, 7, 12));
        setTenantState(recipe, null, true);
        setTenantState(pantryItem, null, true);
        setTenantState(dailyMeal, null, true);

        // Act
        UUID recipeId = recipeRepository.save(recipe).getId();
        UUID pantryItemId = pantryItemRepository.save(pantryItem).getId();
        UUID dailyMealId = dailyMealRepository.save(dailyMeal).getId();
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertTenantState(recipeRepository.findById(recipeId).orElseThrow(), null, true);
        assertTenantState(pantryItemRepository.findById(pantryItemId).orElseThrow(), null, true);
        assertTenantState(dailyMealRepository.findById(dailyMealId).orElseThrow(), null, true);
    }

    private void setTenantState(Object entity, User owner, boolean global) throws Exception {
        Field ownerField = entity.getClass().getDeclaredField("owner");
        ownerField.setAccessible(true);
        ownerField.set(entity, owner);

        Field globalField = entity.getClass().getDeclaredField("global");
        globalField.setAccessible(true);
        globalField.setBoolean(entity, global);
    }

    private void assertTenantState(Object entity, UUID expectedOwnerId, boolean expectedGlobal) throws Exception {
        Field ownerField = entity.getClass().getDeclaredField("owner");
        ownerField.setAccessible(true);
        User owner = (User) ownerField.get(entity);

        Field globalField = entity.getClass().getDeclaredField("global");
        globalField.setAccessible(true);

        assertThat(owner == null ? null : owner.getId()).isEqualTo(expectedOwnerId);
        assertThat(globalField.getBoolean(entity)).isEqualTo(expectedGlobal);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("hsd.inflab.smp.entity")
    @EnableJpaRepositories("hsd.inflab.smp.repository")
    static class RepositoryTestApplication {}
}
