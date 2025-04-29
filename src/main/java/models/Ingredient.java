package models;

public abstract class Ingredient {
    protected final String name;
    protected String unit;          //liter, grams, tablespoons etc.
    protected Double amount;
    protected String category;      //meat, vegetable, spice

    //default constructor for Jackson, needed for deserialization: PantryItem Default Constructor
    public Ingredient() {
        this.name = null;
        this.unit = null;
        this.amount = null;
        this.category = null;
    }

    //only use for recipe ingredient
    public Ingredient (String name) {
        this.name = name;
    }
    public Ingredient(String name, String unit, Double amount, String category) {
        this.name = name;
        this.unit = unit;
        this.amount = amount;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    protected void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getAmount() {
        return amount;
    }

    protected void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        String result = name + ": " + amount + " " + unit;
        if (amount != 1.0)
            result = result + "s";      //add s for plural (1 cup, 1.5 cups, 0.5 cups etc.)

        return result;
    }
}
