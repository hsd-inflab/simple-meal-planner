package models;

public class PantryItem extends Ingredient {
    private String expirationDate;
    private String purchaseDate;
    private String brand;
    private double price;

    //Default-Konstruktor für Jackson
    public PantryItem() {
    }

    public PantryItem (String name, String unit, double amount, String category, String expirationDate, String purchaseDate, String brand, double price) {
        super(name, unit, amount, category);
        this.expirationDate = expirationDate;
        this.purchaseDate = purchaseDate;
        this.brand = brand;
        this.price = price;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getPurchaseDate() {
        return purchaseDate;
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
