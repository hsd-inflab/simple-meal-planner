package hsd.inflab.smp.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import org.springframework.stereotype.Service;

@Service
public class RandomUsernameGenerator {

    private static final List<String> ADJECTIVES =
            List.of("Brave", "Calm", "Clever", "Fresh", "Happy", "Kind", "Quick", "Sunny", "Wise", "Zesty");
    private static final List<String> NOUNS =
            List.of("Tiger", "Panda", "Falcon", "Baker", "Chef", "Apple", "Pepper", "Noodle", "Cookie", "Garden");
    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    private final IntUnaryOperator randomNumberProvider;

    public RandomUsernameGenerator() {
        this(bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    RandomUsernameGenerator(IntUnaryOperator randomNumberProvider) {
        this.randomNumberProvider = randomNumberProvider;
    }

    public String generateUsername() {
        String adjective = randomItem(ADJECTIVES);
        String noun = randomItem(NOUNS);
        int digits = randomNumberProvider.applyAsInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(adjective, noun, digits);
    }

    public List<String> availableAdjectives() {
        return ADJECTIVES;
    }

    public List<String> availableNouns() {
        return NOUNS;
    }

    private String randomItem(List<String> items) {
        return items.get(randomNumberProvider.applyAsInt(items.size()));
    }
}
