package hsd.inflab.smp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipe_book")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<RecipeIngredient> ingredientsPerPerson = new ArrayList<>();

    public Recipe() {}

    public Recipe(String name, List<RecipeIngredient> ingredients) {
        this.name = name;
        this.ingredientsPerPerson = ingredients;
    }

    public Recipe(String name, String description, List<RecipeIngredient> ingredientsPerPerson) {
        this.name = name;
        this.description = description;
        this.ingredientsPerPerson = ingredientsPerPerson;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredientsPerPerson;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredientsPerPerson = ingredients;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredientsPerPerson.add(ingredient);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
