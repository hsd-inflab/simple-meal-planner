package hsd.inflab.smp.service;

import java.util.ArrayList;
import java.util.List;

public final class RandomCredentialVocabulary {

    private static final List<String> ADJECTIVES =
            List.of("Brave", "Calm", "Clever", "Fresh", "Happy", "Kind", "Quick", "Sunny", "Wise", "Zesty");
    private static final List<String> NOUNS =
            List.of("Tiger", "Panda", "Falcon", "Baker", "Chef", "Apple", "Pepper", "Noodle", "Cookie", "Garden");

    private RandomCredentialVocabulary() {}

    public static List<String> adjectives() {
        return ADJECTIVES;
    }

    public static List<String> nouns() {
        return NOUNS;
    }

    public static List<String> words() {
        List<String> words = new ArrayList<>(ADJECTIVES);
        words.addAll(NOUNS);
        return List.copyOf(words);
    }
}
