package hsd.inflab.smp.service;

import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DataService {

    List<PantryItem> loadPantry();

    void savePantry(List<PantryItem> pantry);

    List<Recipe> loadRecipeBook();

    void saveRecipeBook(List<Recipe> recipeBook);

    Map<LocalDate, DailyMeal> loadMealPlans();

    void saveMealPlans(Map<LocalDate, DailyMeal> dailyMealPlans);
}
