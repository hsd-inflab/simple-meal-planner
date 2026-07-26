package hsd.inflab.smp.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Service;

@Service
public class RandomUsernameGenerator implements UsernameGenerator {

    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final RandomGenerator randomGenerator;

    RandomUsernameGenerator() {
        this(new SecureRandom());
    }

    public RandomUsernameGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public String generateUsername() {
        String adjective = randomItem(RandomCredentialVocabulary.adjectives());
        String noun = randomItem(RandomCredentialVocabulary.nouns());
        int digits = randomGenerator.nextInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(adjective, noun, digits);
    }

    private String randomItem(List<String> items) {
        return items.get(randomGenerator.nextInt(items.size()));
    }
}
