package hsd.inflab.smp.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class RandomUsernameGenerator {

    private static final List<String> ADJECTIVES =
            List.of("Brave", "Calm", "Clever", "Fresh", "Happy", "Kind", "Quick", "Sunny", "Wise", "Zesty");
    private static final List<String> NOUNS =
            List.of("Tiger", "Panda", "Falcon", "Baker", "Chef", "Apple", "Pepper", "Noodle", "Cookie", "Garden");
    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    public String generateUsername() {
        String adjective = randomItem(ADJECTIVES);
        String noun = randomItem(NOUNS);
        int digits = ThreadLocalRandom.current().nextInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(adjective, noun, digits);
    }

    public List<String> availableAdjectives() {
        return ADJECTIVES;
    }

    public List<String> availableNouns() {
        return NOUNS;
    }

    private String randomItem(List<String> items) {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }
}
