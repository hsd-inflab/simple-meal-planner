package models;

public class RecipeIngredient extends Ingredient {
    private String foodType;
    private String preparation;

    // ✅ Default-Konstruktor für Jackson
    public RecipeIngredient() {
    }

    public RecipeIngredient(String name, String unit, double amount, String category, String foodType, String preparation) {
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

    @Override
    public String toString() {
        return super.toString() + ", Food Type: " + foodType + ", Preparation: " + preparation;
    }
}
