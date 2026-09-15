package hsd.inflab.smp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class SeededRecipeVisibilityIntegrationTest {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "secret";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void seededRecipes_areVisibleToAuthenticatedUser() throws Exception {
        // Arrange
        String token = login();

        // Act
        MvcResult result = mockMvc.perform(get("/api/recipes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode recipes = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> recipeNames = StreamSupport.stream(recipes.spliterator(), false)
                .map(recipe -> recipe.get("name").asText())
                .toList();
        assertThat(recipeNames).containsExactlyInAnyOrder("Grundbasis Bolognese", "Caffè Latte", "Schnelle Apfeltarte");
    }

    private String login() throws Exception {
        String requestBody =
                objectMapper.writeValueAsString(Map.of("username", DEFAULT_USERNAME, "password", DEFAULT_PASSWORD));
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
