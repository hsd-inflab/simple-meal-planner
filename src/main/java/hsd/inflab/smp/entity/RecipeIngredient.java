package hsd.inflab.smp.entity;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient extends Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String foodType;
    private String preparation;

    public RecipeIngredient() {}

    public RecipeIngredient(
            String name, Unit unit, double amount, Category category, String foodType, String preparation) {
        super(name, unit, amount, category);
        this.foodType = foodType;
        this.preparation = preparation;
    }

    public UUID getId() {
        return id;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }
}
