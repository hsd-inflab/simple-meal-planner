package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class RandomPasswordGeneratorTest {

    @Test
    void generatePassword_returnsWordTwoDigitsAndSpecialCharacter() {
        Queue<Integer> randomValues = new ArrayDeque<>(List.of(12, 42, 0));
        RandomPasswordGenerator generator = new RandomPasswordGenerator(bound -> randomValues.remove());

        String password = generator.generatePassword();

        assertThat(password).matches("^[A-Z][a-z]+\\d{2}[!#$%?]$");
    }

    @Test
    void generatePassword_usesWordFromConfiguredVocabulary() {
        Queue<Integer> randomValues = new ArrayDeque<>(List.of(12, 42, 0));
        RandomPasswordGenerator generator = new RandomPasswordGenerator(bound -> randomValues.remove());

        String password = generator.generatePassword();
        String word = password.substring(0, password.length() - 3);

        assertThat(generator.availableWords()).contains(word);
    }

    @Test
    void availableWords_containsAdjectivesAndNouns() {
        RandomPasswordGenerator generator = new RandomPasswordGenerator(bound -> 0);

        var words = generator.availableWords();

        assertThat(words)
                .hasSize(20)
                .contains("Brave", "Calm", "Clever", "Fresh", "Happy", "Kind", "Quick", "Sunny", "Wise", "Zesty")
                .contains("Tiger", "Panda", "Falcon", "Baker", "Chef", "Apple", "Pepper", "Noodle", "Cookie", "Garden");
    }

    @Test
    void generatePassword_selectsWordDigitsAndSpecialCharacterThroughRandomProvider() {
        Queue<Integer> randomValues = new ArrayDeque<>(List.of(12, 42, 0));
        List<Integer> bounds = new ArrayList<>();
        RandomPasswordGenerator generator = new RandomPasswordGenerator(bound -> {
            bounds.add(bound);
            return randomValues.remove();
        });

        String password = generator.generatePassword();

        assertThat(password).isEqualTo("Falcon42!");
        assertThat(bounds).containsExactly(20, 100, 5);
    }
}
