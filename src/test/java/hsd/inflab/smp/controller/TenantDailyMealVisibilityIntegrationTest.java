package hsd.inflab.smp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hsd.inflab.smp.entity.DailyMeal;
import hsd.inflab.smp.entity.Recipe;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.DailyMealRepository;
import hsd.inflab.smp.repository.RecipeRepository;
import hsd.inflab.smp.repository.UserRepository;
import hsd.inflab.smp.service.DatabaseAutofillerService;
import java.time.LocalDate;
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
class TenantDailyMealVisibilityIntegrationTest {

    private static final String PASSWORD = "test-password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyMealRepository dailyMealRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private DatabaseAutofillerService databaseAutofillerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void getMealPlans_returnsOnlyAuthenticatedUsersMealPlans() throws Exception {
        // Arrange
        User firstUser = saveUser("meal-plan-owner");
        User secondUser = saveUser("other-meal-plan-owner");
        saveDailyMeal(LocalDate.of(2026, 9, 1), firstUser);
        saveDailyMeal(LocalDate.of(2026, 9, 2), secondUser);
        String token = login(firstUser.getUsername());

        // Act
        MvcResult result = mockMvc.perform(get("/api/mealplans").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode mealPlans = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> dates = StreamSupport.stream(mealPlans.spliterator(), false)
                .map(mealPlan -> mealPlan.get("date").asText())
                .toList();
        assertThat(dates).containsExactly("2026-09-01");
    }

    @Test
    void getMealPlanByDate_returnsNotFoundForAnotherUsersMealPlan() throws Exception {
        // Arrange
        User firstUser = saveUser("meal-plan-detail-owner");
        User secondUser = saveUser("other-meal-plan-detail-owner");
        LocalDate date = LocalDate.of(2026, 9, 3);
        saveDailyMeal(date, secondUser);
        String token = login(firstUser.getUsername());

        // Act and Assert
        mockMvc.perform(get("/api/mealplans/{date}", date).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveMealPlan_allowsDifferentUsersToUseSameDate() throws Exception {
        // Arrange
        User firstUser = saveUser("meal-plan-create-owner");
        User secondUser = saveUser("other-meal-plan-create-owner");
        LocalDate date = LocalDate.of(2026, 9, 4);
        String firstUserToken = login(firstUser.getUsername());
        String secondUserToken = login(secondUser.getUsername());

        // Act
        saveMealPlan(firstUserToken, date, 1);
        saveMealPlan(secondUserToken, date, 2);

        // Assert
        mockMvc.perform(get("/api/mealplans/{date}", date).header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakfastServings").value(1));
        mockMvc.perform(get("/api/mealplans/{date}", date).header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakfastServings").value(2));
    }

    @Test
    void saveMealPlan_returnsNotFound_whenRecipeBelongsToAnotherUser() throws Exception {
        // Arrange
        User recipeOwner = saveUser("meal-plan-recipe-owner");
        User otherUser = saveUser("other-meal-plan-recipe-owner");
        Recipe foreignRecipe = new Recipe("Foreign recipe", "Description", List.of());
        foreignRecipe.setOwner(recipeOwner);
        foreignRecipe.setGlobal(false);
        foreignRecipe = recipeRepository.save(foreignRecipe);
        String token = login(otherUser.getUsername());
        String requestBody =
                """
                {
                  "date": "2026-09-05",
                  "breakfastRecipeId": "%s",
                  "lunchRecipeId": null,
                  "dinnerRecipeId": null,
                  "breakfastServings": 1,
                  "lunchServings": 0,
                  "dinnerServings": 0
                }
                """
                        .formatted(foreignRecipe.getId());

        // Act and Assert
        mockMvc.perform(post("/api/mealplans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    private User saveUser(String username) {
        return userRepository.save(new User(username, passwordEncoder.encode(PASSWORD), List.of(Role.USER)));
    }

    private void saveDailyMeal(LocalDate date, User owner) {
        DailyMeal mealPlan = new DailyMeal();
        mealPlan.setMealDate(date);
        mealPlan.setOwner(owner);
        mealPlan.setGlobal(false);
        dailyMealRepository.save(mealPlan);
    }

    private void saveMealPlan(String token, LocalDate date, int breakfastServings) throws Exception {
        String requestBody =
                """
                {
                  "date": "%s",
                  "breakfastRecipeId": null,
                  "lunchRecipeId": null,
                  "dinnerRecipeId": null,
                  "breakfastServings": %d,
                  "lunchServings": 0,
                  "dinnerServings": 0
                }
                """
                        .formatted(date, breakfastServings);
        mockMvc.perform(post("/api/mealplans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
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
