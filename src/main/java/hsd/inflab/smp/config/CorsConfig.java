package hsd.inflab.smp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Gilt für alle URLs (z.B. /api/recipes)
                        .allowedOriginPatterns("*") // Erlaubt Anfragen von jedem Flutter-Port
                        .allowedMethods(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS") // Chrome schickt OPTIONS vor HTTP-Befehle
                        .allowedHeaders("*")
                        .allowCredentials(false); // Verbietet das Mitsenden von Cookies oder sensiblen Login-Daten
            }
        };
    }
}
