package hsd.inflab.smp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "name")
@Entity
@Table(name = "recipe_book")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "is_global", nullable = false)
    private boolean global;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @Setter(AccessLevel.NONE)
    private List<RecipeIngredient> ingredientsPerPerson = new ArrayList<>();

    public Recipe(String name, String description, List<RecipeIngredient> ingredientsPerPerson) {
        this.name = name;
        this.description = description;
        if (ingredientsPerPerson != null) {
            this.ingredientsPerPerson.addAll(ingredientsPerPerson);
        }
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredientsPerPerson.add(ingredient);
    }
}
