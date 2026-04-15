package hsd.inflab.smp.entity;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pantry")
public class PantryItem extends Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private String brand;
    private double price;

    public PantryItem() {}

    public PantryItem(
            String name,
            Unit unit,
            double amount,
            Category category,
            LocalDate expirationDate,
            LocalDate purchaseDate,
            String brand,
            double price) {
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

    public UUID getID() {
        return id;
    }
}
