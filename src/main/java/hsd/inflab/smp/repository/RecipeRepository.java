package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    String INGREDIENTS_GRAPH = "ingredientsPerPerson";

    @Override
    @EntityGraph(attributePaths = {INGREDIENTS_GRAPH})
    List<Recipe> findAll();

    // Global standard recipes are shared by every user and carry no owner.
    List<Recipe> findByGlobalTrue();

    @EntityGraph(attributePaths = {INGREDIENTS_GRAPH})
    @Query(
            """
            SELECT DISTINCT r
            FROM Recipe r
            WHERE NOT EXISTS (
                SELECT ri
                FROM RecipeIngredient ri
                WHERE ri MEMBER OF r.ingredientsPerPerson
                AND NOT EXISTS (
                    SELECT p
                    FROM PantryItem p
                    WHERE LOWER(p.name) = LOWER(ri.name)
                    AND p.amount >= ri.amount
                )
            )
            """)
    List<Recipe> findAvailableRecipes();

    // A recipe is visible when it is a global standard recipe or belongs to the user asking for it.
    @EntityGraph(attributePaths = {INGREDIENTS_GRAPH})
    @Query("SELECT r FROM Recipe r WHERE r.global = true OR r.owner = :owner")
    List<Recipe> findVisibleFor(@Param("owner") User owner);

    @EntityGraph(attributePaths = {INGREDIENTS_GRAPH})
    @Query("SELECT r FROM Recipe r WHERE r.id = :id AND (r.global = true OR r.owner = :owner)")
    Optional<Recipe> findVisibleById(@Param("id") UUID id, @Param("owner") User owner);

    // Same matching as findAvailableRecipes, but restricted to recipes the user may see and to the user's pantry.
    @EntityGraph(attributePaths = {INGREDIENTS_GRAPH})
    @Query(
            """
            SELECT DISTINCT r
            FROM Recipe r
            WHERE (r.global = true OR r.owner = :owner)
            AND NOT EXISTS (
                SELECT ri
                FROM RecipeIngredient ri
                WHERE ri MEMBER OF r.ingredientsPerPerson
                AND NOT EXISTS (
                    SELECT p
                    FROM PantryItem p
                    WHERE LOWER(p.name) = LOWER(ri.name)
                    AND p.amount >= ri.amount
                    AND p.owner = :owner
                )
            )
            """)
    List<Recipe> findAvailableRecipesFor(@Param("owner") User owner);
}
