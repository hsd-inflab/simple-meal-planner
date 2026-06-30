package hsd.inflab.smp.dto.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request-DTO für einen Tagesplan (ohne {@code id} – die ID wird serverseitig vergeben).
 *
 * <p>Rezepte werden ausschließlich per {@code UUID} referenziert (Variante A): Der Tagesplan ordnet bereits
 * bestehende Rezepte zu. Ein {@code null}-Wert bedeutet „keine Mahlzeit für diesen Slot".
 */
public record DailyMealRequestDto(
        LocalDate date,
        UUID breakfastRecipeId,
        UUID lunchRecipeId,
        UUID dinnerRecipeId,
        int breakfastServings,
        int lunchServings,
        int dinnerServings) {}
