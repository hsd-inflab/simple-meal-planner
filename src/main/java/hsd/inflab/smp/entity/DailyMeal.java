package hsd.inflab.smp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
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
@Table(
        name = "daily_meal",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_daily_meal_user_date",
                        columnNames = {"user_id", "meal_date"}))
public class DailyMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "is_global", nullable = false)
    private boolean global;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "breakfast_id")
    private Recipe breakfastRecipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lunch_id")
    private Recipe lunchRecipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dinner_id")
    private Recipe dinnerRecipe;

    @Column(name = "breakfast_servings")
    private int breakfastServings;

    @Column(name = "lunch_servings")
    private int lunchServings;

    @Column(name = "dinner_servings")
    private int dinnerServings;
}
