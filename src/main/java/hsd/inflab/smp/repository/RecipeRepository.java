package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.Recipe;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
