package models;

public class DailyMeal {
    private Recipe breakfast;
    private Recipe lunch;
    private Recipe dinner;
    private int numberOfPersonsBreakfast;
    private int numberOfPersonsLunch;
    private int numberOfPersonsDinner;

    public int getNumberOfPersonsBreakfast() {
        return numberOfPersonsBreakfast;
    }

    public void setNumberOfPersonsBreakfast(int numberOfPersonsBreakfast) {
        this.numberOfPersonsBreakfast = numberOfPersonsBreakfast;
    }

    public int getNumberOfPersonsLunch() {
        return numberOfPersonsLunch;
    }

    public void setNumberOfPersonsLunch(int numberOfPersonsLunch) {
        this.numberOfPersonsLunch = numberOfPersonsLunch;
    }

    public int getNumberOfPersonsDinner() {
        return numberOfPersonsDinner;
    }

    public void setNumberOfPersonsDinner(int numberOfPersonsDinner) {
        this.numberOfPersonsDinner = numberOfPersonsDinner;
    }
    

    public DailyMeal() {
    }

    public DailyMeal(Recipe breakfast, Recipe lunch, Recipe dinner) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }
    
    public DailyMeal(Recipe breakfast, int numberOfPersonsBreakfast, Recipe lunch, int numberOfPersonsLunch, Recipe dinner, int numberOfPersonsDinner) {
        this.breakfast = breakfast;
        this.numberOfPersonsBreakfast = numberOfPersonsBreakfast;
        this.lunch = lunch;
        this.numberOfPersonsLunch = numberOfPersonsLunch;
        this.dinner = dinner;
        this.numberOfPersonsDinner = numberOfPersonsDinner;
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
