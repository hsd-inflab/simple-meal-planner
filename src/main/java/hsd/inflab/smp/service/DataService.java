package hsd.inflab.smp.service;

import hsd.inflab.smp.model.DailyMeal;
import hsd.inflab.smp.model.PantryItem;
import hsd.inflab.smp.model.Recipe;

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