package hsd.inflab.smp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class CredentialGeneratorConfigTest {

    @Test
    void randomPasswordGenerator_usesProvidedSecureRandom() {
        Queue<Integer> randomValues = new ArrayDeque<>(List.of(12, 42, 0));
        List<Integer> bounds = new ArrayList<>();
        SecureRandom secureRandom = new SecureRandom() {
            @Override
            public int nextInt(int bound) {
                bounds.add(bound);
                return randomValues.remove();
            }
        };
        CredentialGeneratorConfig config = new CredentialGeneratorConfig();

        var generator = config.randomPasswordGenerator(secureRandom);
        String password = generator.generatePassword();

        assertThat(password).isEqualTo("Falcon42!");
        assertThat(bounds).containsExactly(20, 100, 5);
    }
}
