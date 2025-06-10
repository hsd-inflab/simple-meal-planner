package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PantryItem extends Ingredient {
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private String brand;
    private double price;

    // Standardformat für Datum als String
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public PantryItem() {
    }

    public PantryItem(String name, String unit, double amount, String category, String expirationDate, String purchaseDate, String brand, double price) {
        super(name, unit, amount, category);
        this.expirationDate = LocalDate.parse(expirationDate, FORMATTER);
        this.purchaseDate = LocalDate.parse(purchaseDate, FORMATTER);
        this.brand = brand;
        this.price = price;
    }

    // Getter gibt String zurück (für JSON)
    public String getExpirationDate() {
        return expirationDate.format(FORMATTER);
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = LocalDate.parse(expirationDate, FORMATTER);
    }

    public String getPurchaseDate() {
        return purchaseDate.format(FORMATTER);
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = LocalDate.parse(purchaseDate, FORMATTER);
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    // consumeIngredient bleibt wie es ist
    public void consumeIngredient(Double amount, String unit) {
        if (unit.equals(getUnit())) {
            setAmount(getAmount() - amount);
        } else {
            throw new IllegalArgumentException("The provided unit does not match.");
        }
    }

    public void printDetails() {
        System.out.println("Pantry Item:");
        System.out.println("  Name: " + name);
        System.out.println("  Amount: " + amount + " " + unit);
        System.out.println("  Category: " + category);
        System.out.println("  Brand: " + brand);
        System.out.println("  Price: " + price + " EUR");
        System.out.println("  Purchase Date: " + purchaseDate);
        System.out.println("  Expiration Date: " + expirationDate);
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
