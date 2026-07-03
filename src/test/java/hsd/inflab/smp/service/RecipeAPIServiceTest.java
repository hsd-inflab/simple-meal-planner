// Testklasse für RecipeAPIService
// Ziel: Überprüft die Logik der Service-Methoden für Rezeptsuche und -details.
// - Nutzt Mockito für die Abhängigkeiten (RecipeApiClient, RecipeApiMapper)
// - Prüft verschiedene Eingabefälle, Fehlerbehandlung und das Mapping der Ergebnisse

package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import hsd.inflab.smp.client.RecipeApiClient;
import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeIngredientDto;
import hsd.inflab.smp.dto.response.RecipeResponseDto;
import hsd.inflab.smp.enums.SearchMode;
import hsd.inflab.smp.mapper.RecipeApiMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeAPIServiceTest {

    // Mock für den API-Client (simuliert externe API-Aufrufe)
    @Mock
    private RecipeApiClient recipeApiClient;

    // Mock für das Mapping von API-Objekten auf interne DTOs
    @Mock
    private RecipeApiMapper recipeApiMapper;

    // Service unter Test, Mocks werden automatisch injiziert
    @InjectMocks
    private RecipeAPIService recipeAPIService;

    @Test
    void searchRecipeTitles_withNullTerms_returnsEmptyListAndDoesNotCallApi() {
        // null als Suchbegriffe -> leere Liste, keine API-Aufrufe
        List<String> result = recipeAPIService.searchRecipeTitles(null, SearchMode.ANY);

        assertTrue(result.isEmpty()); // Erwartung: Ergebnis ist leer

        verifyNoInteractions(recipeApiClient); // API-Client darf nicht aufgerufen werden
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void searchRecipeTitles_withOnlyBlankTerms_returnsEmptyListAndDoesNotCallApi() {
        // nur leere/Whitespace-Suchbegriffe -> leere Liste, keine API-Aufrufe
        List<String> terms = List.of("", "   ", "\t");

        List<String> result = recipeAPIService.searchRecipeTitles(terms, SearchMode.ANY);

        assertTrue(result.isEmpty()); // Erwartung: Ergebnis ist leer

        verifyNoInteractions(recipeApiClient); // API-Client darf nicht aufgerufen werden
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void searchRecipeTitles_withValidTerms_trimsAndJoinsTermsBeforeApiCall() {
        // Begriffe werden getrimmt und zusammengefügt, API wird mit korrektem String aufgerufen
        List<String> terms = List.of(" Tomate ", " Basilikum ");

        // Simuliere ein Rezept, das von der API zurückgegeben wird
        ExternalRecipeDto recipe = new ExternalRecipeDto(
                "Tomate Basilikum Pasta",
                "https://example.com/recipe",
                "image-url",
                List.of(new ExternalRecipeIngredientDto("1", "Tomate", "Stück")));

        // API-Client gibt eine Liste mit einem Rezept zurück
        when(recipeApiClient.searchRecipes("Tomate Basilikum")).thenReturn(List.of(recipe));

        List<String> result = recipeAPIService.searchRecipeTitles(terms, SearchMode.ANY);

        assertEquals(List.of("Tomate Basilikum Pasta"), result); // Erwartung: Titel wird korrekt übernommen

        verify(recipeApiClient).searchRecipes("Tomate Basilikum"); // API-Client wurde mit getrimmtem String aufgerufen
        verifyNoInteractions(recipeApiMapper); // Mapper wird nicht benötigt
    }

    @Test
    void searchRecipeTitles_withDuplicateTitles_returnsDistinctTitles() {
        // Doppelte Titel werden entfernt
        ExternalRecipeDto recipe1 = new ExternalRecipeDto("Pasta", "source-1", "image-1", List.of());

        ExternalRecipeDto recipe2 = new ExternalRecipeDto("Pasta", "source-2", "image-2", List.of());

        ExternalRecipeDto recipe3 = new ExternalRecipeDto("Pizza", "source-3", "image-3", List.of());

        // API-Client gibt Rezepte mit doppeltem Titel zurück
        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of(recipe1, recipe2, recipe3));

        List<String> result = recipeAPIService.searchRecipeTitles(List.of("Pasta"), SearchMode.ANY);

        assertEquals(List.of("Pasta", "Pizza"), result); // Erwartung: Doppelte Titel entfernt
    }

    @Test
    void searchRecipeTitles_withNullAndBlankTitles_filtersThemOut() {
        // Rezepte mit null oder leerem Titel werden herausgefiltert
        ExternalRecipeDto recipe1 = new ExternalRecipeDto(null, "source-1", "image-1", List.of());

        ExternalRecipeDto recipe2 = new ExternalRecipeDto("", "source-2", "image-2", List.of());

        ExternalRecipeDto recipe3 = new ExternalRecipeDto("Kartoffelsuppe", "source-3", "image-3", List.of());

        // API-Client gibt Rezepte mit null/leerem Titel zurück
        when(recipeApiClient.searchRecipes("Kartoffel")).thenReturn(List.of(recipe1, recipe2, recipe3));

        List<String> result = recipeAPIService.searchRecipeTitles(List.of("Kartoffel"), SearchMode.ANY);

        assertEquals(List.of("Kartoffelsuppe"), result); // Erwartung: Nur gültige Titel enthalten
    }

    @Test
    void searchRecipeTitles_whenApiReturnsNull_returnsEmptyList() {
        // API liefert null -> leere Liste
        when(recipeApiClient.searchRecipes("Unbekannt")).thenReturn(null);

        List<String> result = recipeAPIService.searchRecipeTitles(List.of("Unbekannt"), SearchMode.ANY);

        assertTrue(result.isEmpty()); // Erwartung: Ergebnis ist leer
    }

    @Test
    void searchRecipeTitles_whenApiReturnsEmptyList_returnsEmptyList() {
        // API liefert leere Liste -> leere Liste
        when(recipeApiClient.searchRecipes("Unbekannt")).thenReturn(List.of());

        List<String> result = recipeAPIService.searchRecipeTitles(List.of("Unbekannt"), SearchMode.ANY);

        assertTrue(result.isEmpty()); // Erwartung: Ergebnis ist leer
    }

    @Test
    void fetchRecipeData_withNullTitle_returnsNullAndDoesNotCallApi() {
        // null als Titel -> null zurück, keine API-Aufrufe
        RecipeResponseDto result = recipeAPIService.fetchRecipeData(null);

        assertNull(result); // Erwartung: null

        verifyNoInteractions(recipeApiClient); // API-Client darf nicht aufgerufen werden
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void fetchRecipeData_withBlankTitle_returnsNullAndDoesNotCallApi() {
        // leerer Titel -> null zurück, keine API-Aufrufe
        RecipeResponseDto result = recipeAPIService.fetchRecipeData("   ");

        assertNull(result); // Erwartung: null

        verifyNoInteractions(recipeApiClient); // API-Client darf nicht aufgerufen werden
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void fetchRecipeData_whenSearchReturnsNull_returnsNull() {
        // API liefert null -> null zurück
        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(null);

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertNull(result); // Erwartung: null

        verify(recipeApiClient).searchRecipes("Pasta"); // API-Client wurde aufgerufen
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void fetchRecipeData_whenSearchReturnsEmptyList_returnsNull() {
        // API liefert leere Liste -> null zurück
        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of());

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertNull(result); // Erwartung: null

        verify(recipeApiClient).searchRecipes("Pasta"); // API-Client wurde aufgerufen
        verifyNoInteractions(recipeApiMapper); // Mapper darf nicht aufgerufen werden
    }

    @Test
    void fetchRecipeData_withMatchingRecipeWithoutSource_usesSimpleMapper() {
        // Rezept ohne Source-URL -> nur einfaches Mapping
        ExternalRecipeDto matchingRecipe = new ExternalRecipeDto("Pasta", "", "image-url", List.of());

        RecipeResponseDto expectedRecipeDto = mock(RecipeResponseDto.class); // Erwartetes Ergebnis als Mock

        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of(matchingRecipe));

        when(recipeApiMapper.toRecipeDto(matchingRecipe)).thenReturn(expectedRecipeDto);

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertSame(expectedRecipeDto, result); // Erwartung: gemapptes DTO wird zurückgegeben

        verify(recipeApiClient).searchRecipes("Pasta"); // API-Client wurde aufgerufen
        verify(recipeApiClient, never()).crawlRecipe(anyString()); // Kein Crawling-Aufruf
        verify(recipeApiMapper).toRecipeDto(matchingRecipe); // Einfaches Mapping wurde genutzt
    }

    @Test
    void fetchRecipeData_withMatchingRecipeAndCrawledRecipe_usesMapperWithCrawledData() {
        // Rezept mit Source-URL, Crawling liefert Daten -> Mapping mit CrawledData
        ExternalRecipeDto matchingRecipe =
                new ExternalRecipeDto("Pasta", "https://example.com/pasta", "image-url", List.of());

        ExternalCrawlRecipeDto crawledRecipe = new ExternalCrawlRecipeDto(List.of("Step 1", "Step 2"));

        RecipeResponseDto expectedRecipeDto = mock(RecipeResponseDto.class); // Erwartetes Ergebnis als Mock

        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of(matchingRecipe));

        when(recipeApiClient.crawlRecipe("https://example.com/pasta")).thenReturn(crawledRecipe);

        when(recipeApiMapper.toRecipeDto(matchingRecipe, crawledRecipe)).thenReturn(expectedRecipeDto);

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertSame(expectedRecipeDto, result); // Erwartung: gemapptes DTO mit CrawledData

        verify(recipeApiClient).searchRecipes("Pasta"); // API-Client wurde aufgerufen
        verify(recipeApiClient).crawlRecipe("https://example.com/pasta"); // Crawling wurde durchgeführt
        verify(recipeApiMapper).toRecipeDto(matchingRecipe, crawledRecipe); // Mapping mit CrawledData
    }

    @Test
    void fetchRecipeData_whenCrawledRecipeIsNull_usesSimpleMapper() {
        // Crawling liefert null -> einfaches Mapping
        ExternalRecipeDto matchingRecipe =
                new ExternalRecipeDto("Pasta", "https://example.com/pasta", "image-url", List.of());

        RecipeResponseDto expectedRecipeDto = mock(RecipeResponseDto.class); // Erwartetes Ergebnis als Mock

        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of(matchingRecipe));

        when(recipeApiClient.crawlRecipe("https://example.com/pasta")).thenReturn(null);

        when(recipeApiMapper.toRecipeDto(matchingRecipe)).thenReturn(expectedRecipeDto);

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertSame(expectedRecipeDto, result); // Erwartung: gemapptes DTO ohne CrawledData

        verify(recipeApiClient).searchRecipes("Pasta"); // API-Client wurde aufgerufen
        verify(recipeApiClient).crawlRecipe("https://example.com/pasta"); // Crawling wurde durchgeführt
        verify(recipeApiMapper).toRecipeDto(matchingRecipe); // Einfaches Mapping wurde genutzt
        verify(recipeApiMapper, never()).toRecipeDto(any(), any()); // Mapping mit CrawledData wurde nicht genutzt
    }

    @Test
    void fetchRecipeData_whenNoExactTitleMatch_usesFirstSearchResult() {
        // Kein exakter Titel-Treffer -> erster Treffer wird verwendet
        ExternalRecipeDto firstRecipe = new ExternalRecipeDto("Andere Pasta", "", "image-url", List.of());

        ExternalRecipeDto secondRecipe = new ExternalRecipeDto("Noch eine Pasta", "", "image-url", List.of());

        RecipeResponseDto expectedRecipeDto = mock(RecipeResponseDto.class); // Erwartetes Ergebnis als Mock

        when(recipeApiClient.searchRecipes("Pasta")).thenReturn(List.of(firstRecipe, secondRecipe));

        when(recipeApiMapper.toRecipeDto(firstRecipe)).thenReturn(expectedRecipeDto);

        RecipeResponseDto result = recipeAPIService.fetchRecipeData("Pasta");

        assertSame(expectedRecipeDto, result); // Erwartung: erster Treffer wird gemappt

        verify(recipeApiMapper).toRecipeDto(firstRecipe); // Nur erster Treffer wird gemappt
        verify(recipeApiMapper, never()).toRecipeDto(secondRecipe); // Zweiter Treffer wird nicht gemappt
    }
}
