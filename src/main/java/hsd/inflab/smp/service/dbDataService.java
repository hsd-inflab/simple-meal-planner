package hsd.inflab.smp.service;

import hsd.inflab.smp.model.DailyMeal;
import hsd.inflab.smp.model.PantryItem;
import hsd.inflab.smp.model.Recipe;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Deprecated
@Service
public class dbDataService implements DataService {

    public <T> T loadFromFile(String filename, com.fasterxml.jackson.core.type.TypeReference<T> typeReference, T defaultValue) {
        throw new UnsupportedOperationException("dbDataService does not support file operations");
    }

    public <T> void saveToFile(String filename, T data) {
        throw new UnsupportedOperationException("dbDataService does not support file operations");
    }

    @Override
    public List<PantryItem> loadPantry() {
        throw new UnsupportedOperationException("dbDataService does not support pantry operations");
    }

    @Override
    public void savePantry(List<PantryItem> pantry) {
        throw new UnsupportedOperationException("dbDataService does not support pantry operations");
    }

    @Override
    public List<Recipe> loadRecipeBook() {
        return List.of();
    }

    @Override
    public void saveRecipeBook(List<Recipe> recipeBook) {

    }

    @Override
    public Map<LocalDate, DailyMeal> loadMealPlans() {
        return Map.of();
    }

    @Override
    public void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans) {

    }
}
