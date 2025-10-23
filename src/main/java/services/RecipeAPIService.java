package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.*;

import models.Category;
import models.RecipeIngredient;
import models.Unit;

/**
 * service for fetching recipes from the web
 */

public class RecipeAPIService {

    public static class RecipeData {
        private final String title;
        private final String description;
        private final List<RecipeIngredient> ingredients;

        public RecipeData(String title, String description, List<RecipeIngredient> ingredients) {
            this.title = title;
            this.description = description;
            this.ingredients = ingredients;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public List<RecipeIngredient> getIngredients() { return ingredients; }
    }

    private static final String API_BASE = ConfigService.get("recipe.api.base");
    private static final String CRAWL_API_BASE = ConfigService.get("recipe.crawl.base");
    private static final String API_HOST = ConfigService.get("recipe.api.host");
    private static final String API_KEY = ConfigService.get("recipe.api.key");

    public enum SearchMode { INGREDIENTS_ONLY, TITLE_ONLY, ANY }

    public List<String> searchRecipeTitles(List<String> terms) throws Exception {
        return searchRecipeTitles(terms, SearchMode.ANY);
    }

    public List<String> searchRecipeTitles(List<String> terms, SearchMode mode) throws Exception {
        List<String> cleanedTerms = new ArrayList<>();
        if (terms != null) {
            for (String term : terms) {
                String trimmed = term == null ? "" : term.trim();
                if (!trimmed.isEmpty()) {
                    cleanedTerms.add(trimmed);
                }
            }
        }
        if (cleanedTerms.isEmpty()) {
            return List.of();
        }

        String joined = String.join(" ", cleanedTerms);
        String encoded = URLEncoder.encode(joined, Charset.forName("UTF-8"));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE + encoded))
            .header("x-rapidapi-key", API_KEY)
            .header("x-rapidapi-host", API_HOST)
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        String responseBody = response.body();
        return parseRecipeTitlesFiltered(responseBody, cleanedTerms, mode);
    }

    private List<String> parseRecipeTitlesFiltered(String responseBody, List<String> terms, SearchMode mode) {
        List<String> recipeTitles = new ArrayList<>();
        if (responseBody == null || responseBody.isEmpty()) {
            return recipeTitles;
        }
        List<String> lowerTerms = new ArrayList<>();
        for (String t : terms) lowerTerms.add(t.toLowerCase());

        String[] lines = responseBody.split("\"title\":");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int startQuote = line.indexOf("\"");
            int endQuote = line.indexOf("\"", startQuote + 1);
            if (startQuote != -1 && endQuote != -1) {
                String recipeTitle = line.substring(startQuote + 1, endQuote);
                recipeTitle = recipeTitle.replace("\\\"", "\"").replace("\\\\", "\\");
                recipeTitle = decodeUnicode(recipeTitle);
                boolean matches;
                switch (mode) {
                    case TITLE_ONLY: {
                        String titleLower = recipeTitle.toLowerCase();
                        matches = true;
                        for (String t : lowerTerms) { if (!titleLower.contains(t)) { matches = false; break; } }
                        break;
                    }
                    case INGREDIENTS_ONLY: {
                        String ingredientNamesConcat = extractIngredientNamesLower(line);
                        matches = true;
                        for (String t : lowerTerms) { if (!ingredientNamesConcat.contains(t)) { matches = false; break; } }
                        break;
                    }
                    default: {
                        String lineLower = line.toLowerCase();
                        matches = true;
                        for (String t : lowerTerms) { if (!lineLower.contains(t)) { matches = false; break; } }
                    }
                }
                if (matches) {
                    recipeTitles.add(recipeTitle);
                }
            }
        }
        return recipeTitles;
    }

    private String extractIngredientNamesLower(String itemChunk) {
        String lower = itemChunk.toLowerCase();
        StringBuilder names = new StringBuilder();
        int idx = 0;
        String pattern = "\"name\":\"";
        while ((idx = lower.indexOf(pattern, idx)) != -1) {
            int start = idx + pattern.length();
            int end = lower.indexOf("\"", start);
            if (end == -1) break;
            String name = lower.substring(start, end);
            if (!name.isEmpty()) {
                if (names.length() > 0) names.append(' ');
                names.append(name);
            }
            idx = end + 1;
        }
        return names.toString();
    }

    public String getRecipeDetails(String recipeTitle) throws Exception {
        // Delegate to the structured fetch, then format for display.
        RecipeData data = fetchRecipeData(recipeTitle);
        return formatRecipeDetailsFromData(data);
    }

    public RecipeData fetchRecipeData(String recipeTitle) throws Exception {
        String encodedTitle = URLEncoder.encode(recipeTitle, Charset.forName("UTF-8"));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE + encodedTitle))
            .header("x-rapidapi-key", API_KEY)
            .header("x-rapidapi-host", API_HOST)
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        String responseBody = response.body();

        String description = getRecipeDescription(recipeTitle, responseBody);
        List<RecipeIngredient> ingredients = parseIngredientsList(responseBody);
        String decodedDescription = decodeUnicode(description);
        return new RecipeData(recipeTitle, decodedDescription, ingredients);
    }
    
    private String getRecipeDescription(String recipeTitle, String searchResponseBody) {
        try {
            // Extract the recipe URL from the search response
            String recipeUrl = extractRecipeUrl(searchResponseBody, recipeTitle);
            
            if (recipeUrl != null && !recipeUrl.isEmpty()) {
                // Crawl the recipe URL to get the description
                String encodedUrl = URLEncoder.encode(recipeUrl, Charset.forName("UTF-8"));
                
                HttpRequest crawlRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CRAWL_API_BASE + encodedUrl))
                    .header("x-rapidapi-key", API_KEY)
                    .header("x-rapidapi-host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

                HttpResponse<String> crawlResponse = HttpClient.newHttpClient().send(crawlRequest, HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
                String crawlResponseBody = crawlResponse.body();
                String description = parseRecipeDescription(crawlResponseBody);
                
                return description;
            } else { }
        } catch (Exception e) {
            // If crawling fails, return empty description
        }
        return "";
    }
    
    private String extractRecipeUrl(String responseBody, String recipeTitle) {
        // Parse the search response to find the source URL for the specific recipe title
        // Look for "source" parameter which contains the recipe URL
        String[] lines = responseBody.split("\"source\":");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int startQuote = line.indexOf("\"");
            int endQuote = line.indexOf("\"", startQuote + 1);
            if (startQuote != -1 && endQuote != -1) {
                String sourceUrl = line.substring(startQuote + 1, endQuote);
                // For now, we'll take the first source URL found
                // In a more sophisticated approach, you might want to match it with the recipe title
                return sourceUrl;
            }
        }
        return null;
    }
    
    private String parseRecipeDescription(String crawlResponseBody) {
        // Parse the crawled content to extract the recipe description from "steps" parameter
        StringBuilder description = new StringBuilder();
        
        // Look for "steps" parameter which contains the recipe description
        if (crawlResponseBody.contains("\"steps\":")) {
            String[] stepsSections = crawlResponseBody.split("\"steps\":");
            
            if (stepsSections.length > 1) {
                String stepsSection = stepsSections[1];
                
                // Extract the steps content - it might be an array or a string
                if (stepsSection.startsWith("[")) {
                    // Try different parsing approaches for array format
                    
                    // Approach 1: Look for "step" field
                    if (stepsSection.contains("\"step\":")) {
                        String[] steps = stepsSection.split("\"step\":");
                        
                        for (int i = 1; i < steps.length; i++) {
                            String step = steps[i];
                            
                            // Look for the step text between quotes
                            int startQuote = step.indexOf("\"");
                            if (startQuote != -1) {
                                int endQuote = step.indexOf("\"", startQuote + 1);
                                if (endQuote != -1) {
                                    String stepText = step.substring(startQuote + 1, endQuote);
                                    if (!stepText.isEmpty()) {
                                        if (description.length() > 0) {
                                            description.append(" ");
                                        }
                                        description.append(stepText);
                                    }
                                }
                            }
                        }
                    }
                    
                    // Approach 2: If no steps found, try to extract any text content
                    if (description.length() == 0) {
                        // Look for any text content in the array
                        String[] textParts = stepsSection.split("\"");
                        for (int i = 1; i < textParts.length; i += 2) { // Skip every other part (quotes)
                            if (i < textParts.length) {
                                String text = textParts[i];
                                if (!text.isEmpty() && !text.equals("step")) {
                                    if (description.length() > 0) {
                                        description.append(" ");
                                    }
                                    description.append(text);
                                }
                            }
                        }
                    }
                } else {
                    // If steps is a direct string, extract it
                    int startQuote = stepsSection.indexOf("\"");
                    if (startQuote != -1) {
                        int endQuote = stepsSection.indexOf("\"", startQuote + 1);
                        if (endQuote != -1) {
                            String stepsText = stepsSection.substring(startQuote + 1, endQuote);
                            description.append(stepsText);
                        }
                    }
                }
            }
        }
        
        // If no steps found, try alternative patterns
        if (description.length() == 0) {
            String[] alternativePatterns = {
                "\"description\":",
                "\"summary\":",
                "\"instructions\":"
            };
            
            for (String pattern : alternativePatterns) {
                if (crawlResponseBody.contains(pattern)) {
                    int startIndex = crawlResponseBody.indexOf(pattern);
                    if (startIndex != -1) {
                        int contentStart = crawlResponseBody.indexOf("\"", startIndex + pattern.length());
                        if (contentStart != -1) {
                            int contentEnd = crawlResponseBody.indexOf("\"", contentStart + 1);
                            if (contentEnd != -1) {
                                String extractedDescription = crawlResponseBody.substring(contentStart + 1, contentEnd);
                                if (!extractedDescription.isEmpty()) {
                                    description.append(extractedDescription);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return description.toString();
    }

    

    

    private String formatRecipeDetailsFromData(RecipeData data) {
        StringBuilder details = new StringBuilder();
        details.append("Rezept: ").append(data.getTitle()).append("\n\n");

        List<RecipeIngredient> ingredients = data.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            details.append("Zutaten:\n");
            for (RecipeIngredient ri : ingredients) {
                String name = ri.getName() == null ? "" : ri.getName();
                String unit = ri.getUnit().getDisplayName(Locale.GERMAN) == null ? "" : ri.getUnit().getDisplayName(Locale.GERMAN);
                double amount = ri.getAmount();

                if (!name.isEmpty()) {
                    details.append("- ").append(name);
                    if (amount > 0) {
                        // Keep formatting consistent with existing output
                        details.append(": ").append(amount);
                    }
                    if (!unit.isEmpty()) {
                        details.append(" ").append(unit);
                    }
                    details.append("\n");
                }
            }
        }

        String description = data.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            String formattedDescription = formatRecipeDescription(description);
            details.append("\nZubereitung:\n").append(formattedDescription);
        }
        return details.toString();
    }

    private List<RecipeIngredient> parseIngredientsList(String responseBody) {
        List<RecipeIngredient> result = new ArrayList<>();
        String[] ingredientSections = responseBody.split("\"ingredients\":");
        if (ingredientSections.length > 1) {
            String ingredientsSection = ingredientSections[1];
            String[] ingredients = ingredientsSection.split("\\{\"amount\":");
            for (int i = 1; i < ingredients.length; i++) {
                String ingredient = ingredients[i];

                int amountEnd = ingredient.indexOf("\",\"name\":");
                String amountStr = amountEnd != -1 ? ingredient.substring(0, amountEnd).replace("\"", "") : "";

                int nameStart = ingredient.indexOf("\"name\":\"") + 8;
                int nameEnd = ingredient.indexOf("\",\"unit\":");
                String name = (nameStart > 7 && nameEnd != -1) ? ingredient.substring(nameStart, nameEnd) : "";

                int unitStart = ingredient.indexOf("\"unit\":\"") + 8;
                int unitEnd = ingredient.indexOf("\"}", unitStart);
                String unit = (unitStart > 7 && unitEnd != -1) ? ingredient.substring(unitStart, unitEnd) : "";

                name = decodeUnicode(name);
                unit = decodeUnicode(unit);

                //map string to enum value
                Map<String, Unit> unitLookupMap = Unit.getApiLookupMap(Locale.GERMAN);
                Unit inputUnit = unitLookupMap.getOrDefault(unit, Unit.NONE);


                double amount = 0.0;
                try { amount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr); } catch (NumberFormatException ignored) { }

                if (!name.isEmpty()) {
                    RecipeIngredient ri = new RecipeIngredient(name, inputUnit, amount, Category.NONE, "", "");
                    result.add(ri);
                }
            }
        }
        return result;
    }
    


    private String decodeUnicode(String input) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                if (i + 5 < input.length()) {
                    String hex = input.substring(i + 2, i + 6);
                    try {
                        int unicode = Integer.parseInt(hex, 16);
                        result.append((char) unicode);
                        i += 6;
                    } catch (NumberFormatException e) {
                        result.append(input.charAt(i));
                        i++;
                    }
                } else {
                    result.append(input.charAt(i));
                    i++;
                }
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private String formatRecipeDescription(String description) {
        StringBuilder formattedDescription = new StringBuilder();
        
        // Check if the description already contains step-like patterns
        if (description.contains("step") || description.contains("Schritt") || 
            description.contains("1.") || description.contains("2.") || 
            description.contains("First") || description.contains("Then") ||
            description.contains("Next") || description.contains("Finally")) {
            
            // Try to split by common step separators
            String[] steps = description.split("(?<=\\.)\\s+(?=\\d+\\.)|(?<=\\.)\\s+(?=[A-Z])|(?<=\\.)\\s+(?=Then)|(?<=\\.)\\s+(?=Next)|(?<=\\.)\\s+(?=Finally)");
            
            if (steps.length > 1) {
                // Multiple steps found, format them
                for (int i = 0; i < steps.length; i++) {
                    String step = steps[i].trim();
                    if (!step.isEmpty()) {
                        // Remove existing step numbers if present
                        step = step.replaceAll("^\\d+\\.\\s*", "");
                        formattedDescription.append(i + 1).append(". ").append(step).append("\n");
                    }
                }
            } else {
                // Single step or no clear separation, try to break by sentences
                String[] sentences = description.split("(?<=[.!?])\\s+");
                for (int i = 0; i < sentences.length; i++) {
                    String sentence = sentences[i].trim();
                    if (!sentence.isEmpty()) {
                        formattedDescription.append(i + 1).append(". ").append(sentence).append("\n");
                    }
                }
            }
        } else {
            // No clear step pattern, try to break by sentences or natural breaks
            String[] sentences = description.split("(?<=[.!?])\\s+");
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    formattedDescription.append(i + 1).append(". ").append(sentence).append("\n");
                }
            }
        }
        
        // If we still have a very long single step, try to break it further
        if (formattedDescription.toString().split("\n").length <= 2) {
            // Break by commas and "and" for very long descriptions
            String[] parts = description.split("(?<=,)\\s+(?=and)|(?<=,)\\s+(?=und)|(?<=,)\\s+");
            if (parts.length > 2) {
                formattedDescription = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (!part.isEmpty()) {
                        formattedDescription.append(i + 1).append(". ").append(part).append("\n");
                    }
                }
            }
        }
        
        return formattedDescription.toString();
    }
}


