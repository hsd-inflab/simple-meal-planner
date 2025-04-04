package models;

public class RecipeIngredient extends Ingredient{
    private String foodType;
    private String preparation;

    public RecipeIngredient (String name, String unit, double amountPerPerson, String category, String foodType, String preparation) {
        super(name, unit, amountPerPerson, category);
        this.foodType = foodType;
        this.preparation = preparation;
    }

    public String getFoodType() {
        return foodType;
    }

    public String getPreparation() {
        return preparation;
    }

    @Override
    public String toString() {
        return super.toString() + " Food Type: " + foodType + " preparation: " + preparation;
    }
}
