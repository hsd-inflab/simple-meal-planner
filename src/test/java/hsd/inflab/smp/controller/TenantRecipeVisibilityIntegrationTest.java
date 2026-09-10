package hsd.inflab.smp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.PantryItemRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
import hsd.inflab.smp.service.DatabaseAutofillerService;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.security.seed-default-user.enabled=false")
@ActiveProfiles("test")
@Transactional
class TenantRecipeVisibilityIntegrationTest {

    private static final String PASSWORD = "test-password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private DatabaseAutofillerService databaseAutofillerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void getRecipes_returnsOnlyAuthenticatedUsersAndGlobalRecipes() throws Exception {
        // Arrange
        User firstUser = saveUser("recipe-owner");
        User secondUser = saveUser("other-recipe-owner");
        saveRecipe("Owned recipe", firstUser, false);
        saveRecipe("Global recipe", null, true);
        saveRecipe("Foreign recipe", secondUser, false);
        String token = login(firstUser.getUsername());

        // Act
        MvcResult result = mockMvc.perform(get("/api/recipes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode recipes = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> recipeNames = StreamSupport.stream(recipes.spliterator(), false)
                .map(recipe -> recipe.get("name").asText())
                .toList();
        assertThat(recipeNames).containsExactlyInAnyOrder("Owned recipe", "Global recipe");
    }

    @Test
    void getRecipeById_returnsNotFoundForAnotherUsersRecipe() throws Exception {
        // Arrange
        User firstUser = saveUser("recipe-detail-owner");
        User secondUser = saveUser("other-recipe-detail-owner");
        Recipe foreignRecipe = saveRecipe("Foreign detail recipe", secondUser, false);
        String token = login(firstUser.getUsername());

        // Act and Assert
        mockMvc.perform(get("/api/recipes/{id}", foreignRecipe.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAvailableRecipes_usesOnlyVisibleRecipesAndAuthenticatedUsersPantry() throws Exception {
        // Arrange
        User firstUser = saveUser("available-recipe-owner");
        User secondUser = saveUser("other-available-recipe-owner");
        saveRecipe("Owned available recipe", firstUser, false, ingredient("Tomato"));
        saveRecipe("Global available recipe", null, true, ingredient("Rice"));
        saveRecipe("Foreign available recipe", secondUser, false, ingredient("Carrot"));
        saveRecipe("Owned recipe with foreign pantry", firstUser, false, ingredient("Beans"));
        savePantryItem("Tomato", firstUser);
        savePantryItem("Rice", firstUser);
        savePantryItem("Carrot", secondUser);
        savePantryItem("Beans", secondUser);
        String token = login(firstUser.getUsername());

        // Act
        MvcResult result = mockMvc.perform(get("/api/recipes/available").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode recipes = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> recipeNames = StreamSupport.stream(recipes.spliterator(), false)
                .map(recipe -> recipe.get("name").asText())
                .toList();
        assertThat(recipeNames).containsExactlyInAnyOrder("Owned available recipe", "Global available recipe");
    }

    private User saveUser(String username) {
        return userRepository.save(new User(username, passwordEncoder.encode(PASSWORD), List.of(Role.USER)));
    }

    private Recipe saveRecipe(String name, User owner, boolean global) {
        return saveRecipe(name, owner, global, new RecipeIngredient[0]);
    }

    private Recipe saveRecipe(String name, User owner, boolean global, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(name, "Test description", List.of(ingredients));
        recipe.setOwner(owner);
        recipe.setGlobal(global);
        return recipeRepository.save(recipe);
    }

    private RecipeIngredient ingredient(String name) {
        return new RecipeIngredient(name, Unit.G, 100.0, Category.VEGETABLE, "test", "test");
    }

    private void savePantryItem(String name, User owner) {
        PantryItem item = new PantryItem(name, Unit.G, 100.0, Category.VEGETABLE, null, null, "Test brand", 1.0);
        item.setOwner(owner);
        item.setGlobal(false);
        pantryItemRepository.save(item);
    }

    private String login(String username) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("username", username, "password", PASSWORD));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
