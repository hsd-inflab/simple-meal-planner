package hsd.inflab.smp.service;

import java.util.List;
import java.util.function.IntUnaryOperator;

public class RandomUsernameGenerator implements UsernameGenerator {

    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final IntUnaryOperator randomNumberProvider;

    public RandomUsernameGenerator(IntUnaryOperator randomNumberProvider) {
        this.randomNumberProvider = randomNumberProvider;
    }

    @Override
    public String generateUsername() {
        String adjective = randomItem(RandomCredentialVocabulary.adjectives());
        String noun = randomItem(RandomCredentialVocabulary.nouns());
        int digits = randomNumberProvider.applyAsInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(adjective, noun, digits);
    }

    private String randomItem(List<String> items) {
        return items.get(randomNumberProvider.applyAsInt(items.size()));
    }
}
