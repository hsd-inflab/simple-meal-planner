package hsd.inflab.smp.client;

import hsd.inflab.smp.dto.external.ExternalCrawlRecipeDto;
import hsd.inflab.smp.dto.external.ExternalRecipeDto;
import hsd.inflab.smp.service.ConfigService;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RecipeApiClient {

    private final RestClient recipeApiRestClient;

    public RecipeApiClient(ConfigService configService) {
        this.recipeApiRestClient = RestClient.builder()
                .baseUrl(configService.getRecipeApiBase())
                .defaultHeader("X-RapidAPI-Key", configService.getRecipeApiKey())
                .defaultHeader("X-RapidAPI-Host", configService.getRecipeApiHost())
                .build();
    }

    public List<ExternalRecipeDto> searchRecipes(String text) {
        return recipeApiRestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/search_api").queryParam("text", text).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExternalRecipeDto>>() {});
    }

    public ExternalCrawlRecipeDto crawlRecipe(String targetUrl) {
        return recipeApiRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/crawl")
                        .queryParam("target_url", targetUrl)
                        .build())
                .retrieve()
                .body(ExternalCrawlRecipeDto.class);
    }
}
