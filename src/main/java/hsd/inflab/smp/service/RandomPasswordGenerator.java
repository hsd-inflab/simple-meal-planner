package hsd.inflab.smp.service;

import hsd.inflab.smp.util.RandomCredentialVocabulary;
import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Service;

@Service
public class RandomPasswordGenerator {

    private static final List<String> SPECIAL_CHARACTERS = List.of("!", "#", "$", "%", "?");
    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final RandomGenerator randomGenerator;

    RandomPasswordGenerator() {
        this(new SecureRandom());
    }

    public RandomPasswordGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public String generatePassword() {
        String word = randomItem(availableWords());
        int digits = randomGenerator.nextInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        String specialCharacter = randomItem(SPECIAL_CHARACTERS);
        return "%s%02d%s".formatted(word, digits, specialCharacter);
    }

    public List<String> availableWords() {
        return RandomCredentialVocabulary.words();
    }

    private String randomItem(List<String> items) {
        return items.get(randomGenerator.nextInt(items.size()));
    }
}
