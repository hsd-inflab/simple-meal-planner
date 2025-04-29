package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class PantryItem extends Ingredient {
    private Date expirationDate;
    private Date purchaseDate;
    private String brand;
    private double price;

    // ✅ Default-Konstruktor für Jackson (wichtig)
    public PantryItem() {
        // Jackson benötigt den Standardkonstruktor
    }

    // Konstruktor für manuelle Erstellung (schon vorhanden, bleibt so)
    @JsonCreator
    public PantryItem(
            @JsonProperty("name") String name,
            @JsonProperty("unit") String unit,
            @JsonProperty("amount") double amount,
            @JsonProperty("category") String category,
            @JsonProperty("expirationDate") Date expirationDate,
            @JsonProperty("purchaseDate") Date purchaseDate,
            @JsonProperty("brand") String brand,
            @JsonProperty("price") double price
    ) {
        super(name, unit, amount, category);
        this.expirationDate = expirationDate;
        this.purchaseDate = purchaseDate;
        this.brand = brand;
        this.price = price;
    }

    // Getter & Setter für alle Felder
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // consumeIngredient bleibt wie es ist
    public void consumeIngredient(Double amount, String unit) {
        if (unit.equals(getUnit())) {
            setAmount(getAmount() - amount);
        } else {
            throw new IllegalArgumentException("The provided unit does not match.");
        }
    }

    @Override
    public String toString() {
        return(
            super.toString()
            + ", Expiration Date: " + expirationDate
            + ", Purchase Date: " + purchaseDate
            + ", Brand: " + brand
            + ", Price: " + price
        );
    }
}
