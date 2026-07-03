package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class RandomUsernameGeneratorTest {

    @Test
    void generateUsername_returnsAdjectiveNounAndTwoDigits() {
        RandomUsernameGenerator generator = new RandomUsernameGenerator();

        String username = generator.generateUsername();

        assertThat(username).matches("^[A-Z][a-z]+[A-Z][a-z]+\\d{2}$");
    }

    @Test
    void generateUsername_selectsAdjectiveNounAndDigitsThroughRandomProvider() {
        Queue<Integer> randomValues = new ArrayDeque<>(List.of(1, 2, 99));
        List<Integer> bounds = new ArrayList<>();
        RandomUsernameGenerator generator = new RandomUsernameGenerator(bound -> {
            bounds.add(bound);
            return randomValues.remove();
        });

        String username = generator.generateUsername();

        assertThat(username).isEqualTo("CalmFalcon99");
        assertThat(bounds).containsExactly(10, 10, 100);
    }
}
