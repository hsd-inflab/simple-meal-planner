package hsd.inflab.smp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        var json = "{\"username\":\"admin\",\"password\":\"secret\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        var json = "{\"username\":\"admin\",\"password\":\"wrong\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/pantry")).andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_withToken_returns200() throws Exception {
        var loginJson = "{\"username\":\"admin\",\"password\":\"secret\"}";

        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = res.getResponse().getContentAsString();
        assertThat(responseBody).contains("token");

        // extract token naive
        String token = responseBody.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/pantry").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void registerRandomUser_returnsCreatedCredentialsAndToken() throws Exception {
        MvcResult registrationResult = mockMvc.perform(post("/api/auth/register/random"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username")
                        .value(org.hamcrest.Matchers.matchesPattern("^[A-Z][a-z]+[A-Z][a-z]+\\d{2}$")))
                .andExpect(jsonPath("$.password")
                        .value(org.hamcrest.Matchers.matchesPattern("^[A-Z][a-z]+\\d{2}[!#$%?]$")))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        JsonNode registrationResponse =
                objectMapper.readTree(registrationResult.getResponse().getContentAsString());
        String username = registrationResponse.get("username").asText();
        String password = registrationResponse.get("password").asText();
        String loginJson =
                """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void registerRandomUser_persistsCreatedUserInDatabase() throws Exception {
        MvcResult registrationResult = mockMvc.perform(post("/api/auth/register/random"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode registrationResponse =
                objectMapper.readTree(registrationResult.getResponse().getContentAsString());
        String username = registrationResponse.get("username").asText();
        String password = registrationResponse.get("password").asText();

        var savedUser = userRepository.findByUsername(username);

        assertThat(savedUser).isPresent();
        assertThat(savedUser.orElseThrow().getUsername()).isEqualTo(username);
        assertThat(savedUser.orElseThrow().getPasswordHash()).isNotEqualTo(password);
        assertThat(savedUser.orElseThrow().getRoles()).containsExactly(Role.USER);
    }
}
