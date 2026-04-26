package hsd.inflab.smp.service.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.service.ConfigService;
import hsd.inflab.smp.service.DailyMealService;
import hsd.inflab.smp.service.MealPlannerService;
import hsd.inflab.smp.service.PantryService;
import hsd.inflab.smp.service.PasswordService;
import hsd.inflab.smp.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MealPlannerServiceTest {

    // Mock für die Konfiguration, aus der der API-Hash gelesen wird.
    @Mock
    private ConfigService configService;

    // Mock für die eigentliche Passwortprüfung per Hashvergleich.
    @Mock
    private PasswordService passwordService;

    // Diese Services sind im Konstruktor als Abhängigkeiten vorhanden und werden hier gemockt.
    @Mock
    private PantryService pantryService;

    // Ebenfalls nur für das Wiring im Konstruktor relevant.
    @Mock
    private RecipeService recipeService;

    // Ebenfalls nur für das Wiring im Konstruktor relevant.
    @Mock
    private DailyMealService dailyMealService;

    // Mockito baut den Service automatisch mit den Mocks zusammen, damit die Methoden isoliert testbar sind.
    @InjectMocks
    private MealPlannerService mealPlannerService;

    @Test
    void verifyAPIPassword_whenPasswordMatches_returnsTrue() {
        // Arrange: Eingabewert und erwarteten Hash festlegen.
        String password = "secret";
        String passwordHash = "hashed-secret";

        // Der ConfigService liefert den gespeicherten API-Hash.
        when(configService.getRecipeApiPasswordhash()).thenReturn(passwordHash);
        // Der PasswordService soll für genau diese Kombination true zurückgeben.
        when(passwordService.verifyPassword(password, passwordHash)).thenReturn(true);

        // Act: Den Service mit dem Klartext-Passwort aufrufen.
        boolean result = mealPlannerService.verifyAPIPassword(password);

        // Assert: Das Ergebnis kommt unverändert vom PasswordService.
        assertTrue(result);
        // Zusätzlich prüfen wir, dass beide Abhängigkeiten genau wie erwartet benutzt wurden.
        verify(configService).getRecipeApiPasswordhash();
        verify(passwordService).verifyPassword(password, passwordHash);
    }

    @Test
    void verifyAPIPassword_whenPasswordDoesNotMatch_returnsFalse() {
        // Arrange: falsches Passwort, aber derselbe gespeicherte Hash.
        String password = "wrong";
        String passwordHash = "hashed-secret";

        // Der gespeicherte Hash bleibt gleich.
        when(configService.getRecipeApiPasswordhash()).thenReturn(passwordHash);
        // Für diese Eingabe soll die Prüfung fehlschlagen.
        when(passwordService.verifyPassword(password, passwordHash)).thenReturn(false);

        // Act: Prüfung mit einem falschen Passwort.
        boolean result = mealPlannerService.verifyAPIPassword(password);

        // Assert: Das Ergebnis muss false sein.
        assertFalse(result);
        // Auch hier sollen die Abhängigkeiten korrekt aufgerufen werden.
        verify(configService).getRecipeApiPasswordhash();
        verify(passwordService).verifyPassword(password, passwordHash);
    }
}
