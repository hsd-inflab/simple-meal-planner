package hsd.inflab.smp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ConfigService {

    @Value("${pantry.file}")
    private String pantryFile;

    @Value("${recipebook.file}")
    private String recipebookFile;

    @Value("${mealplans.file}")
    private String mealplansFile;

    @Value("${temp.file}")
    private String tempFile;

    @Value("${recipe.api.key}")
    private String recipeApiKey;

    @Value("${recipe.api.base}")
    private String recipeApiBase;

    @Value("${recipe.crawl.base}")
    private String recipeCrawlBase;

    @Value("${recipe.api.host}")
    private String recipeApiHost;

    @Value("${recipe.api.passwordhash}")
    private String recipeApiPasswordhash;

    public String getPantryFile() {
        return pantryFile;
    }

    public String getRecipebookFile() {
        return recipebookFile;
    }

    public String getMealplansFile() {
        return mealplansFile;
    }

    public String getTempFile() {
        return tempFile;
    }

    public String getRecipeApiKey() {
        return recipeApiKey;
    }

    public String getRecipeApiBase() {
        return recipeApiBase;
    }

    public String getRecipeCrawlBase() {
        return recipeCrawlBase;
    }

    public String getRecipeApiHost() {
        return recipeApiHost;
    }

    public String getRecipeApiPasswordhash() {
        return recipeApiPasswordhash;
    }
}