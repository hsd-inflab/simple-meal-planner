package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DBDataService implements DataService {

    @Override
    public List<PantryItem> loadPantry() {
        return List.of(); // TODO: implement with repository
    }

    @Override
    public void savePantry(List<PantryItem> pantry) {
        // TODO: implement with repository
    }

    @Override
    public List<Recipe> loadRecipeBook() {
        return List.of(); // TODO: implement with repository
    }

    @Override
    public void saveRecipeBook(List<Recipe> recipeBook) {
        // TODO: implement with repository
    }

    @Override
    public Map<LocalDate, DailyMeal> loadMealPlans() {
        return Map.of(); // TODO: implement with repository
    }

    @Override
    public void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {
        // TODO: implement with repository
    }
}
