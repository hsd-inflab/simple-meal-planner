package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.request.PantryItemRequestDto;
import hsd.inflab.smp.dto.response.PantryItemResponseDto;
import hsd.inflab.smp.entity.PantryItem;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import hsd.inflab.smp.mapper.PantryItemMapper;
import hsd.inflab.smp.mapper.PantryItemMapperImpl;
import hsd.inflab.smp.repository.PantryItemRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PantryServiceTest {

    // Repository wird isoliert gemockt, damit nur die Service-Logik getestet wird.
    @Mock
    private PantryItemRepository pantryRepo;

    // Das Mapping uebernimmt der echte Mapper (siehe PantryItemMapperTest).
    @Spy
    private PantryItemMapper pantryItemMapper = new PantryItemMapperImpl();

    // Mockito injiziert die Mocks/Spies automatisch in den zu testenden Service.
    @InjectMocks
    private PantryService pantryService;

    /**
     * Prueft den Erfolgsfall von getPantry.
     *
     * Wenn das Repository Elemente liefert, muessen diese korrekt in DTOs gemappt
     * und in der Ergebnisliste zurueckgegeben werden.
     */
    @Test
    void getPantry_whenRepositoryHasItems_returnsMappedDtos() {
        // Arrange: Testdaten fuer ein Pantry-Element vorbereiten.
        UUID id = UUID.randomUUID();
        LocalDate expirationDate = LocalDate.of(2026, 5, 1);
        LocalDate purchaseDate = LocalDate.of(2026, 4, 20);

        // Entity wird hier gemockt, damit alle Getter-Werte exakt kontrolliert werden koennen.
        PantryItem pantryItem = mock(PantryItem.class);

        // Mock-Verhalten fuer die Entity-Getter festlegen.
        when(pantryItem.getId()).thenReturn(id);
        when(pantryItem.getName()).thenReturn("Reis");
        when(pantryItem.getUnit()).thenReturn(Unit.KG);
        when(pantryItem.getAmount()).thenReturn(1.5);
        when(pantryItem.getCategory()).thenReturn(Category.STARCH);
        when(pantryItem.getExpirationDate()).thenReturn(expirationDate);
        when(pantryItem.getPurchaseDate()).thenReturn(purchaseDate);
        when(pantryItem.getBrand()).thenReturn("BioMarke");
        when(pantryItem.getPrice()).thenReturn(3.99);

        // Repository liefert genau dieses eine Element.
        when(pantryRepo.findAll()).thenReturn(List.of(pantryItem));

        // Act: Service-Methode ausfuehren.
        List<PantryItemResponseDto> result = pantryService.getPantry();

        // Assert: Erwartetes DTO fuer den Mapping-Vergleich aufbauen.
        PantryItemResponseDto expected = new PantryItemResponseDto(
                id, "Reis", Unit.KG, 1.5, Category.STARCH, expirationDate, purchaseDate, "BioMarke", 3.99);

        // Assert: Liste enthaelt genau einen korrekt gemappten Eintrag.
        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));

        // Interaktionspruefung: findAll muss genau im Erfolgsweg genutzt werden.
        verify(pantryRepo).findAll();
    }

    /**
     * Prueft den Grenzfall von getPantry.
     *
     * Wenn das Repository leer ist, muss die Rueckgabe ebenfalls eine leere Liste sein.
     */
    @Test
    void getPantry_whenRepositoryIsEmpty_returnsEmptyList() {
        // Arrange: Repository liefert keine Pantry-Elemente.
        when(pantryRepo.findAll()).thenReturn(List.of());

        // Act: Service-Methode ausfuehren.
        List<PantryItemResponseDto> result = pantryService.getPantry();

        // Assert: Ergebnisliste muss leer sein.
        assertTrue(result.isEmpty());

        // Interaktionspruefung: Lesen ja, Speichern nein.
        verify(pantryRepo).findAll();
        verify(pantryRepo, never()).save(any(PantryItem.class));
    }

    /**
     * Prueft den Erfolgsfall von addItem.
     *
     * Ein gueltiges DTO wird gespeichert und als DTO mit den Werten des gespeicherten
     * Entities (inklusive ID) zurueckgegeben.
     */
    @Test
    void addItem_whenValidDto_returnsSavedDto() {
        // Arrange: Eingabe-DTO simuliert einen gueltigen neuen Vorratseintrag.
        LocalDate expirationDate = LocalDate.of(2026, 6, 15);
        LocalDate purchaseDate = LocalDate.of(2026, 4, 25);
        PantryItemRequestDto input = new PantryItemRequestDto(
                "Milch", Unit.L, 1.0, Category.DAIRY, expirationDate, purchaseDate, "Hofgut", 1.49);

        // Das gespeicherte Entity kommt mit generierter ID aus dem Repository zurueck.
        UUID savedId = UUID.randomUUID();
        PantryItem savedEntity = mock(PantryItem.class);

        // Rueckgabewerte des gespeicherten Entities fuer das Rueck-Mapping definieren.
        when(savedEntity.getId()).thenReturn(savedId);
        when(savedEntity.getName()).thenReturn("Milch");
        when(savedEntity.getUnit()).thenReturn(Unit.L);
        when(savedEntity.getAmount()).thenReturn(1.0);
        when(savedEntity.getCategory()).thenReturn(Category.DAIRY);
        when(savedEntity.getExpirationDate()).thenReturn(expirationDate);
        when(savedEntity.getPurchaseDate()).thenReturn(purchaseDate);
        when(savedEntity.getBrand()).thenReturn("Hofgut");
        when(savedEntity.getPrice()).thenReturn(1.49);

        // Mock-Verhalten: save liefert das vorbereitete gespeicherte Entity.
        when(pantryRepo.save(any(PantryItem.class))).thenReturn(savedEntity);

        // Act: DTO ueber den Service speichern.
        PantryItemResponseDto result = pantryService.addItem(input);

        // Assert: Rueckgabe entspricht den Werten des gespeicherten Entities.
        assertEquals(savedId, result.id());
        assertEquals("Milch", result.name());
        assertEquals(Unit.L, result.unit());
        assertEquals(1.0, result.amount());
        assertEquals(Category.DAIRY, result.category());

        // Interaktionspruefung: Es muss ein Save-Aufruf erfolgt sein.
        verify(pantryRepo).save(any(PantryItem.class));
    }
}
