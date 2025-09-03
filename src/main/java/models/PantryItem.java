package models;

import java.time.LocalDate;

public class PantryItem extends Ingredient {
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private String brand;
    private double price;

    public PantryItem() {
    }

    public PantryItem(String name, Unit unit, double amount, Category category, LocalDate expirationDate, LocalDate purchaseDate, String brand, double price) {
        super(name, unit, amount, category);
        this.expirationDate = expirationDate;
        this.purchaseDate = purchaseDate;
        this.brand = brand;
        this.price = price;
    }

    // Getter gibt String zurück (für JSON)
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    // consumeIngredient bleibt wie es ist
    public void consumeIngredient(Double amount, Unit unit) {
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
        System.out.println("  Purchase Date: " + formatAsGermanDate(purchaseDate));
        System.out.println("  Expiration Date: " + formatAsGermanDate(expirationDate));
    }
}
