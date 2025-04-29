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


    public void printDetails() {
        System.out.println("Recipe Ingredient:");
        System.out.println("  Name: " + name);
        System.out.println("  Amount per Person: " + amount + " " + unit);
        System.out.println("  Category: " + category);
        System.out.println("  Food Type: " + foodType);
        System.out.println("  Preparation: " + preparation);
    }

    @Override
    public String toString() {
        return super.toString() + " Food Type: " + foodType + " preparation: " + preparation;
    }
}
