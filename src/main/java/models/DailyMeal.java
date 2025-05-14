package models;

public class DailyMeal {
    private Recipe breakfast;
    private Recipe lunch;
    private Recipe dinner;

    public DailyMeal() {
    }

    public DailyMeal(Recipe breakfast, Recipe lunch, Recipe dinner) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public Recipe getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(Recipe breakfast) {
        this.breakfast = breakfast;
    }

    public Recipe getLunch() {
        return lunch;
    }

    public void setLunch(Recipe lunch) {
        this.lunch = lunch;
    }

    public Recipe getDinner() {
        return dinner;
    }

    public void setDinner(Recipe dinner) {
        this.dinner = dinner;
    }

    /* public int getTotalCalories() {
        int total = 0;
        if (breakfast != null) total += breakfast.getCalories();
        if (lunch != null) total += lunch.getCalories();
        if (dinner != null) total += dinner.getDinner();
        return total;
    } */

    public void printDetails() {
        System.out.println("Daily Meal Details:");
        
        System.out.print("Breakfast: ");
        if (breakfast != null) {
            System.out.println(breakfast.getName());
        } else {
            System.out.println("Not planned");
        }
        
        System.out.print("Lunch: ");
        if (lunch != null) {
            System.out.println(lunch.getName());
        } else {
            System.out.println("Not planned");
        }
        
        System.out.print("Dinner: ");
        if (dinner != null) {
            System.out.println(dinner.getName());
        } else {
            System.out.println("Not planned");
        }
    }

    @Override
    public String toString() {
        return "DailyMeal{" +
                "breakfast=" + breakfast +
                ", lunch=" + lunch +
                ", dinner=" + dinner +
                '}';
    }
}
