package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Creates the personal starter data of a newly registered user: a filled pantry and a few meal plans. Recipes are
 * not copied - the plans reference the shared global standard recipes seeded by the autofiller.
 *
 * <p>Two switches control this: {@code app.starter-data.enabled} decides whether the feature exists at all, and
 * {@code app.starter-data.roles} decides which roles receive the data. The roles are matched against the entries
 * stored in {@code app_user_roles} for that user.
 */
@Service
@ConditionalOnProperty(prefix = "app.starter-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserStarterDataService {

    private final PantryItemRepository pantryItemRepository;
    private final DailyMealRepository dailyMealRepository;
    private final RecipeRepository recipeRepository;
    private final List<Role> rolesReceivingStarterData;

    public UserStarterDataService(
            PantryItemRepository pantryItemRepository,
            DailyMealRepository dailyMealRepository,
            RecipeRepository recipeRepository,
            @Value("${app.starter-data.roles:USER}") List<Role> rolesReceivingStarterData) {
        this.pantryItemRepository = pantryItemRepository;
        this.dailyMealRepository = dailyMealRepository;
        this.recipeRepository = recipeRepository;
        this.rolesReceivingStarterData = List.copyOf(rolesReceivingStarterData);
    }

    public void createStarterDataFor(User owner) {
        if (!receivesStarterData(owner)) {
            return;
        }

        pantryItemRepository.saveAll(starterPantry(owner));

        List<Recipe> globalRecipes = recipeRepository.findByGlobalTrue();
        if (globalRecipes.isEmpty()) {
            // Without standard recipes there is nothing to plan. The pantry alone is still a valid starting point.
            return;
        }

        dailyMealRepository.saveAll(starterMealPlans(owner, globalRecipes));
    }

    // Matches the configured roles against the roles stored for this user in app_user_roles.
    private boolean receivesStarterData(User owner) {
        List<Role> ownerRoles = owner.getRoles();
        if (ownerRoles == null) {
            return false;
        }
        return ownerRoles.stream().anyMatch(rolesReceivingStarterData::contains);
    }

    private List<PantryItem> starterPantry(User owner) {
        PantryItem pasta = new PantryItem(
                "Spaghetti",
                Unit.G,
                500.0,
                Category.STARCH,
                LocalDate.now().plusYears(1),
                LocalDate.now(),
                "Barilla",
                1.99);

        PantryItem tomatoSauce = new PantryItem(
                "Tomatensauce",
                Unit.UNIT,
                2.0,
                Category.VEGETABLE,
                LocalDate.now().plusMonths(6),
                LocalDate.now(),
                "Oro di Parma",
                2.49);

        PantryItem flour = new PantryItem(
                "Weizenmehl",
                Unit.KG,
                2.5,
                Category.STARCH,
                LocalDate.now().plusMonths(6),
                LocalDate.now(),
                "Diamant",
                1.99);

        PantryItem eggs = new PantryItem(
                "Eier",
                Unit.UNIT,
                30.0,
                Category.DAIRY,
                LocalDate.now().plusMonths(12),
                LocalDate.now(),
                "Fuerstenhof",
                7.49);

        return List.of(ownedBy(pasta, owner), ownedBy(tomatoSauce, owner), ownedBy(flour, owner), ownedBy(eggs, owner));
    }

    private List<DailyMeal> starterMealPlans(User owner, List<Recipe> globalRecipes) {
        DailyMeal todayPlan = new DailyMeal();
        todayPlan.setMealDate(LocalDate.now());
        todayPlan.setBreakfastRecipe(recipeAt(globalRecipes, 0));
        todayPlan.setBreakfastServings(1);
        todayPlan.setLunchRecipe(recipeAt(globalRecipes, 1));
        todayPlan.setLunchServings(2);

        DailyMeal nextDayPlan = new DailyMeal();
        nextDayPlan.setMealDate(LocalDate.now().plusDays(1));
        nextDayPlan.setBreakfastRecipe(recipeAt(globalRecipes, 0));
        nextDayPlan.setBreakfastServings(2);
        nextDayPlan.setLunchRecipe(recipeAt(globalRecipes, 1));
        nextDayPlan.setLunchServings(1);

        DailyMeal nextTwoDaysPlan = new DailyMeal();
        nextTwoDaysPlan.setMealDate(LocalDate.now().plusDays(2));
        nextTwoDaysPlan.setDinnerRecipe(recipeAt(globalRecipes, 2));
        nextTwoDaysPlan.setDinnerServings(2);

        return List.of(ownedBy(todayPlan, owner), ownedBy(nextDayPlan, owner), ownedBy(nextTwoDaysPlan, owner));
    }

    // Wraps around so the template also works with fewer standard recipes than slots.
    private Recipe recipeAt(List<Recipe> globalRecipes, int index) {
        return globalRecipes.get(index % globalRecipes.size());
    }

    private PantryItem ownedBy(PantryItem item, User owner) {
        item.setOwner(owner);
        item.setGlobal(false);
        return item;
    }

    private DailyMeal ownedBy(DailyMeal meal, User owner) {
        meal.setOwner(owner);
        meal.setGlobal(false);
        return meal;
    }
}
