package hsd.inflab.smp.repository;

import hsd.inflab.smp.entity.Recipe;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Override
    @EntityGraph(attributePaths = {"ingredientsPerPerson"})
    List<Recipe> findAll();
}
