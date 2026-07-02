package hsd.inflab.smp.service;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class RandomUsernameGenerator {

    private static final String ADJECTIVE = "Brave";
    private static final String NOUN = "Tiger";
    private static final int MAX_TWO_DIGIT_NUMBER_EXCLUSIVE = 100;

    public String generateUsername() {
        int digits = ThreadLocalRandom.current().nextInt(MAX_TWO_DIGIT_NUMBER_EXCLUSIVE);
        return "%s%s%02d".formatted(ADJECTIVE, NOUN, digits);
    }
}
