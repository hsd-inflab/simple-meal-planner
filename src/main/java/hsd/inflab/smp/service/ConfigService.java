package hsd.inflab.smp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigService {

    @Value("${recipe.api.key}")
    private String recipeApiKey;

    @Value("${recipe.api.base}")
    private String recipeApiBase;

    @Value("${recipe.api.host}")
    private String recipeApiHost;

    @Value("${recipe.api.passwordhash}")
    private String recipeApiPasswordhash;

    public String getRecipeApiKey() {
        return recipeApiKey;
    }

    public String getRecipeApiBase() {
        return recipeApiBase;
    }

    public String getRecipeApiHost() {
        return recipeApiHost;
    }

    public String getRecipeApiPasswordhash() {
        return recipeApiPasswordhash;
    }
}
