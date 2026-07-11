package hsd.inflab.smp.config;

import hsd.inflab.smp.service.RandomPasswordGenerator;
import hsd.inflab.smp.service.RandomUsernameGenerator;
import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CredentialGeneratorConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    public RandomPasswordGenerator randomPasswordGenerator(SecureRandom secureRandom) {
        return new RandomPasswordGenerator(secureRandom::nextInt);
    }

    @Bean
    public RandomUsernameGenerator randomUsernameGenerator(SecureRandom secureRandom) {
        return new RandomUsernameGenerator(secureRandom::nextInt);
    }
}
