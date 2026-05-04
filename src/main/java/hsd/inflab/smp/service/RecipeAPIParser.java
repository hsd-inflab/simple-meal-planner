package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.RecipeAPIDto;
import hsd.inflab.smp.entity.RecipeIngredient;
import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.SearchMode;
import hsd.inflab.smp.enums.Unit;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RecipeAPIParser {

    private static final int MIN_STEPS_SECTIONS_COUNT = 1;
    private static final int MIN_INGREDIENT_SECTIONS_COUNT = 1;
    private static final int MULTIPLE_STEPS_MIN_COUNT = 1;

    private final ConfigService configService;
    private final HttpClient httpClient;

    public RecipeAPIParser(ConfigService configService) {
        this.configService = configService;
        this.httpClient = HttpClient.newHttpClient(); // HttpClient initialisieren
    }

    public List<String> parseRecipeTitlesFiltered(String responseBody, List<String> terms, SearchMode mode) {
        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }

        List<String> lowerTerms = toLowerTerms(terms);
        List<String> recipeTitles = new ArrayList<>();

        String[] chunks = responseBody.split("\"title\":");

        for (int i = 1; i < chunks.length; i++) {
            String chunk = chunks[i];

            Optional<String> title = extractRecipeTitle(chunk);

            if (title.isPresent() && matchesSearchMode(title.get(), chunk, lowerTerms, mode)) {
                recipeTitles.add(title.get());
            }
        }

        return recipeTitles;
    }

    public List<String> toLowerTerms(List<String> terms) {
        if (terms == null) {
            return Collections.emptyList();
        }

        List<String> lowerTerms = new ArrayList<>();

        for (String term : terms) {
            if (term != null && !term.isBlank()) {
                lowerTerms.add(term.toLowerCase(Locale.ROOT));
            }
        }

        return lowerTerms;
    }

    public Optional<String> extractRecipeTitle(String line) {
        int startQuote = line.indexOf("\"");
        int endQuote = line.indexOf("\"", startQuote + 1);

        if (startQuote == -1 || endQuote == -1) {
            return Optional.empty();
        }

        String recipeTitle = line.substring(startQuote + 1, endQuote);
        recipeTitle = recipeTitle.replace("\\\"", "\"").replace("\\\\", "\\");

        recipeTitle = decodeUnicode(recipeTitle);

        return Optional.of(recipeTitle);
    }

    public boolean matchesSearchMode(String recipeTitle, String line, List<String> lowerTerms, SearchMode mode) {
        String searchableText;

        switch (mode) {
            case TITLE_ONLY:
                searchableText = recipeTitle.toLowerCase(Locale.ROOT);
                break;

            case INGREDIENTS_ONLY:
                searchableText = extractIngredientNamesLower(line);
                break;

            default:
                searchableText = line.toLowerCase(Locale.ROOT);
                break;
        }

        return containsAllTerms(searchableText, lowerTerms);
    }

    public boolean containsAllTerms(String text, List<String> lowerTerms) {
        for (String term : lowerTerms) {
            if (!text.contains(term)) {
                return false;
            }
        }

        return true;
    }

    public String extractIngredientNamesLower(String itemChunk) {
        String lower = itemChunk.toLowerCase(Locale.ROOT);
        StringBuilder names = new StringBuilder();

        String pattern = "\"name\":\"";
        int index = lower.indexOf(pattern);

        while (index != -1) {
            int start = index + pattern.length();
            int end = lower.indexOf("\"", start);

            if (end == -1) {
                break;
            }

            String name = lower.substring(start, end);

            if (!name.isEmpty()) {
                if (names.length() > 0) {
                    names.append(' ');
                }

                names.append(name);
            }

            index = lower.indexOf(pattern, end + 1);
        }

        return names.toString();
    }

    public String getRecipeDescription(String recipeTitle, String searchResponseBody) {
        try {
            // Extract the recipe URL from the search response
            String recipeUrl = extractRecipeUrl(searchResponseBody, recipeTitle);

            if (recipeUrl != null && !recipeUrl.isEmpty()) {
                // Methode: HTTP-Anfrage an die extrahierte URL, um die Rezeptbeschreibung zu erhalten
                String crawlResponseBody = requestRecipeAPI(
                        recipeUrl,
                        configService
                                .getRecipeCrawlBase()); // URL wird sowohl als Suchbegriff als auch als URL übergeben

                return parseRecipeDescription(crawlResponseBody);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Abruf der Rezeptbeschreibung wurde unterbrochen: " + e.getMessage());
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Abruf der Rezeptbeschreibung fehlgeschlagen: " + e.getMessage());
        }

        return "";
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    public String extractRecipeUrl(String responseBody, String recipeTitle) {
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

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    public String parseRecipeDescription(String crawlResponseBody) {
        // Parse the crawled content to extract the recipe description from "steps" parameter
        StringBuilder description = new StringBuilder();

        // Look for "steps" parameter which contains the recipe description
        if (crawlResponseBody.contains("\"steps\":")) {
            String[] stepsSections = crawlResponseBody.split("\"steps\":");

            if (stepsSections.length > MIN_STEPS_SECTIONS_COUNT) {
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
                            if (startQuote == -1) {
                                continue;
                            }

                            int endQuote = step.indexOf("\"", startQuote + 1);
                            if (endQuote == -1) {
                                continue;
                            }

                            String stepText = step.substring(startQuote + 1, endQuote);
                            if (stepText.isEmpty()) {
                                continue;
                            }

                            if (!description.isEmpty()) {
                                description.append(" ");
                            }
                            description.append(stepText);
                        }
                    }

                    // Approach 2: If no steps found, try to extract any text content
                    if (description.isEmpty()) {
                        // Look for any text content in the array
                        String[] textParts = stepsSection.split("\"");
                        for (int i = 1; i < textParts.length; i += 2) { // Skip every other part (quotes)
                            String text = textParts[i];
                            if (!text.isEmpty() && !"step".equals(text)) {
                                if (!description.isEmpty()) {
                                    description.append(" ");
                                }
                                description.append(text);
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
        if (description.isEmpty()) {
            String[] alternativePatterns = {"\"description\":", "\"summary\":", "\"instructions\":"};

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

    public String formatRecipeDetailsFromData(RecipeAPIDto data) {
        StringBuilder details = new StringBuilder();
        details.append("Rezept: ").append(data.title()).append("\n\n");

        List<RecipeIngredient> ingredients = data.ingredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            details.append("Zutaten:\n");
            for (RecipeIngredient ri : ingredients) {
                String name = ri.getName() == null ? "" : ri.getName();
                String unit = ri.getUnit().getDisplayName(Locale.GERMAN) == null
                        ? ""
                        : ri.getUnit().getDisplayName(Locale.GERMAN);
                double amount = ri.getAmount();

                if (!name.isEmpty()) {
                    details.append("- ").append(name);
                    if (amount > 0) {
                        details.append(": ").append(amount);
                    }
                    if (!unit.isEmpty()) {
                        details.append(" ").append(unit);
                    }
                    details.append("\n");
                }
            }
        }

        String description = data.description();
        if (description != null && !description.trim().isEmpty()) {
            String formattedDescription = formatRecipeDescription(description);
            details.append("\nZubereitung:\n").append(formattedDescription);
        }
        return details.toString();
    }

    public List<RecipeIngredient> parseIngredientsList(String responseBody) {
        List<RecipeIngredient> result = new ArrayList<>();
        String[] ingredientSections = responseBody.split("\"ingredients\":");
        if (ingredientSections.length > MIN_INGREDIENT_SECTIONS_COUNT) {
            String ingredientsSection = ingredientSections[1];
            String[] ingredients = ingredientsSection.split("\\{\"amount\":");
            for (int i = 1; i < ingredients.length; i++) {
                String ingredient = ingredients[i];

                int amountEnd = ingredient.indexOf("\",\"name\":");
                String amountStr =
                        amountEnd != -1 ? ingredient.substring(0, amountEnd).replace("\"", "") : "";

                int nameStart = ingredient.indexOf("\"name\":\"") + 8;
                int nameEnd = ingredient.indexOf("\",\"unit\":");
                String name = (nameStart > 7 && nameEnd != -1) ? ingredient.substring(nameStart, nameEnd) : "";

                int unitStart = ingredient.indexOf("\"unit\":\"") + 8;
                int unitEnd = ingredient.indexOf("\"}", unitStart);
                String unit = (unitStart > 7 && unitEnd != -1) ? ingredient.substring(unitStart, unitEnd) : "";

                name = decodeUnicode(name);
                unit = decodeUnicode(unit);

                // map string to enum value
                Map<String, Unit> unitLookupMap = Unit.getApiLookupMap(Locale.GERMAN);
                Unit inputUnit = unitLookupMap.getOrDefault(unit, Unit.NONE);

                double amount = 0.0;
                try {
                    amount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
                } catch (NumberFormatException ignored) {
                }

                if (!name.isEmpty()) {
                    RecipeIngredient ri = new RecipeIngredient(name, inputUnit, amount, Category.NONE, "", "");
                    result.add(ri);
                }
            }
        }
        return result;
    }

    public String decodeUnicode(String input) {
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

    public String formatRecipeDescription(String description) {
        StringBuilder formattedDescription = new StringBuilder();

        // Check if the description already contains step-like patterns
        if (description.contains("step")
                || description.contains("Schritt")
                || description.contains("1.")
                || description.contains("2.")
                || description.contains("First")
                || description.contains("Then")
                || description.contains("Next")
                || description.contains("Finally")) {

            // Try to split by common step separators
            String[] steps = description.split(
                    "(?<=\\.)\\s+(?=\\d+\\.)|(?<=\\.)\\s+(?=[A-Z])|(?<=\\.)\\s+(?=Then)|(?<=\\.)\\s+(?=Next)|(?<=\\.)\\s+(?=Finally)");

            if (steps.length > MULTIPLE_STEPS_MIN_COUNT) {
                // Multiple steps found, format them
                for (int i = 0; i < steps.length; i++) {
                    String step = steps[i].trim();
                    if (!step.isEmpty()) {
                        // Remove existing step numbers if present
                        step = step.replaceAll("^\\d+\\.\\s*", "");
                        formattedDescription
                                .append(i + 1)
                                .append(". ")
                                .append(step)
                                .append("\n");
                    }
                }
            } else {
                // Single step or no clear separation, try to break by sentences
                String[] sentences = description.split("(?<=[.!?])\\s+");
                for (int i = 0; i < sentences.length; i++) {
                    String sentence = sentences[i].trim();
                    if (!sentence.isEmpty()) {
                        formattedDescription
                                .append(i + 1)
                                .append(". ")
                                .append(sentence)
                                .append("\n");
                    }
                }
            }
        } else {
            // No clear step pattern, try to break by sentences or natural breaks
            String[] sentences = description.split("(?<=[.!?])\\s+");
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    formattedDescription
                            .append(i + 1)
                            .append(". ")
                            .append(sentence)
                            .append("\n");
                }
            }
        }

        // If we still have a very long single step, try to break it further
        final int maxStepsThreshold = 2;
        final int minPartsForBreakdown = 2;

        if (formattedDescription.toString().split("\n").length <= maxStepsThreshold) {
            // Break by commas and "and" for very long descriptions
            String[] parts = description.split("(?<=,)\\s+(?=and)|(?<=,)\\s+(?=und)|(?<=,)\\s+");
            if (parts.length > minPartsForBreakdown) {
                formattedDescription = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (!part.isEmpty()) {
                        formattedDescription
                                .append(i + 1)
                                .append(". ")
                                .append(part)
                                .append("\n");
                    }
                }
            }
        }

        return formattedDescription.toString();
    }

    // Methode: Erstellung einer HTTP-Anfrage an die Rezept-API mit einem Suchbegriff und Rückgabe der Antwort als
    // String
    public String requestRecipeAPI(String term, String base)
            throws IOException, InterruptedException { // base: Basis-URL für unterschiedliche Zieladressen
        // Umwandlung des Suchbegriffs in URL-kodiertes Format, um Sonderzeichen zu handhaben
        String encoded = URLEncoder.encode(term, StandardCharsets.UTF_8);

        // Anfrage an die API wird gebaut
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + encoded)) // Basis-URL + Suchbegriffe
                .header("x-rapidapi-key", configService.getRecipeApiKey()) // API-Zugangsdaten: Key
                .header("x-rapidapi-host", configService.getRecipeApiHost()) // API-Zugangsdaten: Host
                .method("GET", HttpRequest.BodyPublishers.noBody()) // GET-Anfrage
                .build();

        // Gebaute Anfrage wird abgeschickt und die Antwort als String (z.B. als JSON) empfangen
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        return response.body();
    }
}
