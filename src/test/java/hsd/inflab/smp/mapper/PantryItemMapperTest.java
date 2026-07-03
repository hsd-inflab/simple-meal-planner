package hsd.inflab.smp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PantryItemMapperTest {

    private final PantryItemMapper mapper = new PantryItemMapperImpl();

    @Test
    void toEntity_mapsAllFields_withoutId() {
        LocalDate expirationDate = LocalDate.of(2026, 7, 10);
        LocalDate purchaseDate = LocalDate.of(2026, 4, 18);
        PantryItemRequestDto dto = new PantryItemRequestDto(
                "Olivenoel", Unit.ML, 500.0, Category.FATS, expirationDate, purchaseDate, "Mediterran", 7.25);

        PantryItem result = mapper.toEntity(dto);

        assertNull(result.getId());
        assertEquals("Olivenoel", result.getName());
        assertEquals(Unit.ML, result.getUnit());
        assertEquals(500.0, result.getAmount());
        assertEquals(Category.FATS, result.getCategory());
        assertEquals(expirationDate, result.getExpirationDate());
        assertEquals(purchaseDate, result.getPurchaseDate());
        assertEquals("Mediterran", result.getBrand());
        assertEquals(7.25, result.getPrice());
    }

    @Test
    void toDto_mapsAllFields() {
        PantryItem entity = new PantryItem(
                "Reis",
                Unit.KG,
                1.5,
                Category.STARCH,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 4, 20),
                "Bio",
                3.99);

        PantryItemResponseDto result = mapper.toDto(entity);

        assertEquals("Reis", result.name());
        assertEquals(Unit.KG, result.unit());
        assertEquals(1.5, result.amount());
        assertEquals(Category.STARCH, result.category());
        assertEquals("Bio", result.brand());
        assertEquals(3.99, result.price());
    }

    @Test
    void mappingNullReturnsNull() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
