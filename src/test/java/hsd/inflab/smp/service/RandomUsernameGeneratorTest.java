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

    @Test
    void availableAdjectives_returnsTenAdjectives() {
        RandomUsernameGenerator generator = new RandomUsernameGenerator();

        var adjectives = generator.availableAdjectives();

        assertThat(adjectives).hasSize(10);
    }

    @Test
    void availableNouns_returnsTenNouns() {
        RandomUsernameGenerator generator = new RandomUsernameGenerator();

        var nouns = generator.availableNouns();

        assertThat(nouns).hasSize(10);
    }

    @Test
    void generateUsername_usesConfiguredAdjectiveAndNoun() {
        RandomUsernameGenerator generator = new RandomUsernameGenerator();

        String username = generator.generateUsername();
        String wordsOnly = username.substring(0, username.length() - 2);

        boolean usesConfiguredWords = generator.availableAdjectives().stream().anyMatch(adjective -> {
            if (!wordsOnly.startsWith(adjective)) {
                return false;
            }
            String noun = wordsOnly.substring(adjective.length());
            return generator.availableNouns().contains(noun);
        });

        assertThat(usesConfiguredWords).isTrue();
    }
}
