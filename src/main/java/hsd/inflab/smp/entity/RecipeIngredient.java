package hsd.inflab.smp.entity;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient extends Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String foodType;
    private String preparation;

    public RecipeIngredient(
            String name, Unit unit, double amount, Category category, String foodType, String preparation) {
        super(name, unit, amount, category);
        this.foodType = foodType;
        this.preparation = preparation;
    }
}
