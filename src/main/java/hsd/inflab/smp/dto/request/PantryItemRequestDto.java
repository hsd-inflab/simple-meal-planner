package hsd.inflab.smp.dto.request;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import java.time.LocalDate;

/**
 * Request-DTO für einen Vorratseintrag (ohne {@code id} – die ID wird serverseitig vergeben).
 */
public record PantryItemRequestDto(
        String name,
        Unit unit,
        Double amount,
        Category category,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        String brand,
        double price) {}
