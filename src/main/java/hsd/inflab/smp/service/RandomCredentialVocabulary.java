package hsd.inflab.smp.service;

import java.util.ArrayList;
import java.util.List;

public final class RandomCredentialVocabulary {

    private static final List<String> ADJECTIVE_WORDS =
            List.of("Brave", "Calm", "Clever", "Fresh", "Happy", "Kind", "Quick", "Sunny", "Wise", "Zesty");
    private static final List<String> NOUN_WORDS =
            List.of("Tiger", "Panda", "Falcon", "Baker", "Chef", "Apple", "Pepper", "Noodle", "Cookie", "Garden");

    private RandomCredentialVocabulary() {}

    public static List<String> adjectives() {
        return ADJECTIVE_WORDS;
    }

    public static List<String> nouns() {
        return NOUN_WORDS;
    }

    public static List<String> words() {
        List<String> words = new ArrayList<>(ADJECTIVE_WORDS);
        words.addAll(NOUN_WORDS);
        return List.copyOf(words);
    }
}
