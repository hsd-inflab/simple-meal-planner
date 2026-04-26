package hsd.inflab.smp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hsd.inflab.smp.dto.PantryItemDto;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.service.PantryService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PantryControllerTest {
    private MockMvc mockMvc;
    private PantryService pantryService;

    @BeforeEach
    void setUp() {
        pantryService = Mockito.mock(PantryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PantryController(pantryService))
                .build();
    }

    @Test
    void getPantryReturnsStoredItems() throws Exception {
        PantryItemDto item = new PantryItemDto(
                UUID.randomUUID(),
                "Tomate",
                Unit.UNIT,
                2.0,
                Category.VEGETABLE,
                LocalDate.of(2026, 4, 25),
                LocalDate.of(2026, 4, 20),
                "Bio",
                1.99);
        when(pantryService.getPantry()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/pantry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomate"))
                .andExpect(jsonPath("$[0].category").value("VEGETABLE"));
    }

    @Test
    void addItemReturnsCreatedResponse() throws Exception {
        UUID itemId = UUID.randomUUID();
        String requestJson =
                """
                {
                  "name": "Milch",
                  "unit": "L",
                  "amount": 1.0,
                  "category": "DAIRY",
                  "expirationDate": "2026-04-22",
                  "purchaseDate": "2026-04-20",
                  "brand": "Marke",
                  "price": 1.49
                }
                """;
        PantryItemDto requestDto = new PantryItemDto(
                null,
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        PantryItemDto responseDto = new PantryItemDto(
                itemId,
                "Milch",
                Unit.L,
                1.0,
                Category.DAIRY,
                LocalDate.of(2026, 4, 22),
                LocalDate.of(2026, 4, 20),
                "Marke",
                1.49);
        when(pantryService.addItem(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/pantry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pantry/" + itemId))
                .andExpect(jsonPath("$.id").value(itemId.toString()));
    }

    @Test
    void deleteItemReturnsNoContentForExistingItem() throws Exception {
        UUID itemId = UUID.randomUUID();
        PantryItemDto item = new PantryItemDto(
                itemId, "Apfel", Unit.UNIT, 4.0, Category.FRUIT, null, LocalDate.of(2026, 4, 20), "Obsthof", 2.50);
        when(pantryService.getItemById(itemId)).thenReturn(Optional.of(item));

        mockMvc.perform(delete("/api/pantry/{id}", itemId)).andExpect(status().isNoContent());

        verify(pantryService).deleteItem(itemId);
    }
}
