package hsd.inflab.smp.service;

import java.util.List;
import java.util.function.IntUnaryOperator;

public class RandomPasswordGenerator {

    private static final List<String> SPECIAL_CHARACTERS = List.of("!", "#", "$", "%", "?");
    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final IntUnaryOperator randomNumberProvider;

    public RandomPasswordGenerator(IntUnaryOperator randomNumberProvider) {
        this.randomNumberProvider = randomNumberProvider;
    }

    public String generatePassword() {
        String word = randomItem(availableWords());
        int digits = randomNumberProvider.applyAsInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        String specialCharacter = randomItem(SPECIAL_CHARACTERS);
        return "%s%02d%s".formatted(word, digits, specialCharacter);
    }

    public List<String> availableWords() {
        return RandomCredentialVocabulary.words();
    }

    private String randomItem(List<String> items) {
        return items.get(randomNumberProvider.applyAsInt(items.size()));
    }
}
