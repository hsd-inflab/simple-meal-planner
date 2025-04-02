package models;

import java.util.Date;

public class Ingredient {
    private final String name;
    private String measuringType;
    private Double amount;
    private final Date expirationDate;

    //only use for recipe ingredient
    public Ingredient (String name) {
        this.name = name;
        this.expirationDate = new Date();
    }
    public Ingredient(String name, String measuringType, Double amount, Date expirationDate) {
        this.name = name;
        this.measuringType = measuringType;
        this.amount = amount;
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public String getMeasuringType() {
        return measuringType;
    }

    protected void setMeasuringType(String measuringType) {
        this.measuringType = measuringType;
    }

    public Double getAmount() {
        return amount;
    }

    protected void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void consumeIngredient(Double amount, String measuringType) {
        if (measuringType.equals(getMeasuringType()))
            setAmount(getAmount() - amount);
        else throw new IllegalArgumentException("The provided measuring type does not match.");
    }

    @Override
    public String toString() {
        return name + measuringType + amount + expirationDate;
    }
}
