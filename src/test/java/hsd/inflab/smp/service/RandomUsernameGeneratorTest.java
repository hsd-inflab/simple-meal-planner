package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RandomUsernameGeneratorTest {

    @Test
    void generateUsername_returnsAdjectiveNounAndTwoDigits() {
        RandomUsernameGenerator generator = new RandomUsernameGenerator();

        String username = generator.generateUsername();

        assertThat(username).matches("^[A-Z][a-z]+[A-Z][a-z]+\\d{2}$");
    }
}
