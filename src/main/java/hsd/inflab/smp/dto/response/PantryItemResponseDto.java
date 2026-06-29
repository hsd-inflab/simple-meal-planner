package hsd.inflab.smp.dto.response;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.time.LocalDate;
import java.util.UUID;

public record PantryItemResponseDto(
        UUID id,
        String name,
        Unit unit,
        Double amount,
        Category category,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        String brand,
        double price) {
    @Override
    public String toString() {
        return name();
    }
}
