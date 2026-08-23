package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.Recipe;
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

    @Override
    @EntityGraph(attributePaths = {"ingredientsPerPerson"})
    List<Recipe> findAll();

    @EntityGraph(attributePaths = {"ingredientsPerPerson"})
    @Query(
            """
            SELECT DISTINCT r
            FROM Recipe r
            LEFT JOIN r.owner owner
            WHERE r.global = true OR owner.username = :username
            """)
    List<Recipe> findVisibleToUser(@Param("username") String username);

    @EntityGraph(attributePaths = {"ingredientsPerPerson"})
    @Query(
            """
            SELECT DISTINCT r
            FROM Recipe r
            LEFT JOIN r.owner owner
            WHERE r.id = :id
            AND (r.global = true OR owner.username = :username)
            """)
    Optional<Recipe> findVisibleById(@Param("id") UUID id, @Param("username") String username);

    @EntityGraph(attributePaths = {"ingredientsPerPerson"})
    @Query(
            """
            SELECT DISTINCT r
            FROM Recipe r
            LEFT JOIN r.owner recipeOwner
            WHERE (r.global = true OR recipeOwner.username = :username)
            AND NOT EXISTS (
                SELECT ri
                FROM RecipeIngredient ri
                WHERE ri MEMBER OF r.ingredientsPerPerson
                AND NOT EXISTS (
                    SELECT p
                    FROM PantryItem p
                    LEFT JOIN p.owner pantryOwner
                    WHERE LOWER(p.name) = LOWER(ri.name)
                    AND p.amount >= ri.amount
                    AND (p.global = true OR pantryOwner.username = :username)
                )
            )
            """)
    List<Recipe> findAvailableRecipes(@Param("username") String username);
}
