package hsd.inflab.smp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_meal")
public class DailyMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "meal_date", unique = true, nullable = false)
    private LocalDate mealDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "breakfast_id")
    private Recipe breakfastRecipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lunch_id")
    private Recipe lunchRecipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dinner_id")
    private Recipe dinnerRecipe;

    @Column(name = "breakfast_servings")
    private int breakfastServings;

    @Column(name = "lunch_servings")
    private int lunchServings;

    @Column(name = "dinner_servings")
    private int dinnerServings;

    public DailyMeal() {}

    public DailyMeal(LocalDate date, Recipe breakfast, Recipe lunch, Recipe dinner) {
        this.mealDate = date;
        this.breakfastRecipe = breakfast;
        this.lunchRecipe = lunch;
        this.dinnerRecipe = dinner;
    }

    public DailyMeal(
            LocalDate date,
            Recipe breakfast,
            int breakfastServings,
            Recipe lunch,
            int lunchServings,
            Recipe dinner,
            int dinnerServings) {
        this.mealDate = date;
        this.breakfastRecipe = breakfast;
        this.breakfastServings = breakfastServings;
        this.lunchRecipe = lunch;
        this.lunchServings = lunchServings;
        this.dinnerRecipe = dinner;
        this.dinnerServings = dinnerServings;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate date) {
        this.mealDate = date;
    }

    public int getBreakfastServings() {
        return breakfastServings;
    }

    public void setBreakfastServings(int breakfastServings) {
        this.breakfastServings = breakfastServings;
    }

    public int getLunchServings() {
        return lunchServings;
    }

    public void setLunchServings(int lunchServings) {
        this.lunchServings = lunchServings;
    }

    public int getDinnerServings() {
        return dinnerServings;
    }

    public void setDinnerServings(int dinnerServings) {
        this.dinnerServings = dinnerServings;
    }

    public Recipe getBreakfastRecipe() {
        return breakfastRecipe;
    }

    public void setBreakfastRecipe(Recipe breakfast) {
        this.breakfastRecipe = breakfast;
    }

    public Recipe getLunchRecipe() {
        return lunchRecipe;
    }

    public void setLunchRecipe(Recipe lunch) {
        this.lunchRecipe = lunch;
    }

    public Recipe getDinnerRecipe() {
        return dinnerRecipe;
    }

    public void setDinnerRecipe(Recipe dinner) {
        this.dinnerRecipe = dinner;
    }
}
