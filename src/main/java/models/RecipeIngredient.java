package models;

public class RecipeIngredient extends Ingredient {
    private String foodType;
    private String preparation;

    // ✅ Default-Konstruktor für Jackson
    public RecipeIngredient() {
    }

    public RecipeIngredient(String name, Unit unit, double amount, Category category, String foodType, String preparation) {
        super(name, unit, amount, category);
        this.foodType = foodType;
        this.preparation = preparation;
    }

    // ✅ Getter & Setter
    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public void printDetails() {
        System.out.println("Recipe Ingredient:");
        System.out.println("  Name: " + name);
        System.out.println("  Amount per Person: " + amount + " " + unit);
        System.out.println("  Category: " + category);
        System.out.println("  Food Type: " + foodType);
        System.out.println("  Preparation: " + preparation);
    }
}
