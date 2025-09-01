package models;

import models.Category;
import models.Unit;

public abstract class Ingredient {

    protected final String name;
    protected Unit unit;          //liter, grams, tablespoons etc.
    protected Double amount;
    protected Category category;      //meat, vegetable, spice

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
    public Ingredient(String name, Unit unit, Double amount, Category category) {
        this.name = name;
        this.unit = unit;
        this.amount = amount;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }

    protected void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Double getAmount() {
        return amount;
    }

    protected void setAmount(Double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
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
