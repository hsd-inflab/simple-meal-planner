package hsd.inflab.smp.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RandomCredentialVocabularyTest {

    @Test
    void availableAdjectives_returnsTenAdjectives() {
        var adjectives = RandomCredentialVocabulary.adjectives();

        assertThat(adjectives).hasSize(10);
    }

    @Test
    void availableNouns_returnsTenNouns() {
        var nouns = RandomCredentialVocabulary.nouns();

        assertThat(nouns).hasSize(10);
    }

    @Test
    void words_containsConfiguredAdjectivesAndNouns() {
        var words = RandomCredentialVocabulary.words();

        assertThat(words)
                .containsAll(RandomCredentialVocabulary.adjectives())
                .containsAll(RandomCredentialVocabulary.nouns());
    }
}
