package hsd.inflab.smp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.repository.PantryItemRepository;
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
class TenantPantryVisibilityIntegrationTest {

    private static final String PASSWORD = "test-password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

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
    void getPantry_returnsOnlyAuthenticatedUsersAndGlobalItems() throws Exception {
        // Arrange
        User firstUser = saveUser("pantry-owner");
        User secondUser = saveUser("other-pantry-owner");
        savePantryItem("Owned item", firstUser, false);
        savePantryItem("Global item", null, true);
        savePantryItem("Foreign item", secondUser, false);
        String token = login(firstUser.getUsername());

        // Act
        MvcResult result = mockMvc.perform(get("/api/pantry").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString());
        List<String> itemNames = StreamSupport.stream(items.spliterator(), false)
                .map(item -> item.get("name").asText())
                .toList();
        assertThat(itemNames).containsExactlyInAnyOrder("Owned item", "Global item");
    }

    @Test
    void getPantryItemById_returnsNotFoundForAnotherUsersItem() throws Exception {
        // Arrange
        User firstUser = saveUser("pantry-detail-owner");
        User secondUser = saveUser("other-pantry-detail-owner");
        PantryItem foreignItem = savePantryItem("Foreign detail item", secondUser, false);
        String token = login(firstUser.getUsername());

        // Act and Assert
        mockMvc.perform(get("/api/pantry/{id}", foreignItem.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpiredItems_deletesOnlyAuthenticatedUsersExpiredItems() throws Exception {
        // Arrange
        User firstUser = saveUser("pantry-expiration-owner");
        User secondUser = saveUser("other-pantry-expiration-owner");
        LocalDate expired = LocalDate.now().minusDays(1);
        LocalDate valid = LocalDate.now().plusDays(1);
        savePantryItem("Owned expired item", firstUser, false, expired);
        savePantryItem("Owned valid item", firstUser, false, valid);
        savePantryItem("Foreign expired item", secondUser, false, expired);
        savePantryItem("Global expired item", null, true, expired);
        String firstUserToken = login(firstUser.getUsername());
        String secondUserToken = login(secondUser.getUsername());

        // Act
        mockMvc.perform(delete("/api/pantry/expired").header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isNoContent());

        // Assert
        assertThat(getPantryItemNames(firstUserToken))
                .containsExactlyInAnyOrder("Owned valid item", "Global expired item");
        assertThat(getPantryItemNames(secondUserToken))
                .containsExactlyInAnyOrder("Foreign expired item", "Global expired item");
    }

    @Test
    void deleteExpiredItems_withoutAuthentication_returnsUnauthorized() throws Exception {
        // Act and Assert
        mockMvc.perform(delete("/api/pantry/expired")).andExpect(status().isUnauthorized());
    }

    private User saveUser(String username) {
        return userRepository.save(new User(username, passwordEncoder.encode(PASSWORD), List.of(Role.USER)));
    }

    private PantryItem savePantryItem(String name, User owner, boolean global) {
        return savePantryItem(name, owner, global, null);
    }

    private PantryItem savePantryItem(String name, User owner, boolean global, LocalDate expirationDate) {
        PantryItem item = new PantryItem(name, Unit.UNIT, 1.0, Category.NONE, expirationDate, null, "Test brand", 1.0);
        item.setOwner(owner);
        item.setGlobal(global);
        return pantryItemRepository.save(item);
    }

    private List<String> getPantryItemNames(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/pantry").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString());
        return StreamSupport.stream(items.spliterator(), false)
                .map(item -> item.get("name").asText())
                .toList();
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
