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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pantry")
public class PantryItem extends Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private String brand;
    private double price;

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
}
