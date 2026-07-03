package hsd.inflab.smp.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import org.springframework.stereotype.Service;

@Service
public class RandomUsernameGenerator implements UsernameGenerator {

    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final IntUnaryOperator randomNumberProvider;

    public RandomUsernameGenerator() {
        this(bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    RandomUsernameGenerator(IntUnaryOperator randomNumberProvider) {
        this.randomNumberProvider = randomNumberProvider;
    }

    @Override
    public String generateUsername() {
        String adjective = randomItem(RandomCredentialVocabulary.adjectives());
        String noun = randomItem(RandomCredentialVocabulary.nouns());
        int digits = randomNumberProvider.applyAsInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(adjective, noun, digits);
    }

    public List<String> availableAdjectives() {
        return RandomCredentialVocabulary.adjectives();
    }

    public List<String> availableNouns() {
        return RandomCredentialVocabulary.nouns();
    }

    private String randomItem(List<String> items) {
        return items.get(randomNumberProvider.applyAsInt(items.size()));
    }
}
