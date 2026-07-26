package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.util.RandomCredentialVocabulary;
import java.util.Arrays;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.stereotype.Service;

class RandomPasswordGeneratorTest {

    @Test
    void generatePassword_returnsWordTwoDigitsAndSpecialCharacter() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.nextInt(anyInt())).thenReturn(12, 42, 0);
        RandomPasswordGenerator generator = new RandomPasswordGenerator(randomGenerator);

        String password = generator.generatePassword();

        assertThat(password).matches("^[A-Z][a-z]+\\d{2}[!#$%?]$");
    }

    @Test
    void generatePassword_usesWordFromConfiguredVocabulary() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.nextInt(anyInt())).thenReturn(12, 42, 0);
        RandomPasswordGenerator generator = new RandomPasswordGenerator(randomGenerator);

        String password = generator.generatePassword();
        String word = password.substring(0, password.length() - 3);

        assertThat(RandomCredentialVocabulary.words()).contains(word);
    }

    @Test
    void generator_doesNotExposeVocabulary() {
        var publicMethods = Arrays.asList(RandomPasswordGenerator.class.getMethods());

        boolean exposesVocabulary =
                publicMethods.stream().anyMatch(method -> method.getName().equals("availableWords"));

        assertThat(exposesVocabulary).isFalse();
    }

    @Test
    void generatePassword_selectsWordDigitsAndSpecialCharacterThroughRandomProvider() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.nextInt(anyInt())).thenReturn(12, 42, 0);
        RandomPasswordGenerator generator = new RandomPasswordGenerator(randomGenerator);

        String password = generator.generatePassword();

        assertThat(password).isEqualTo("Falcon42!");
        InOrder randomCalls = inOrder(randomGenerator);
        randomCalls.verify(randomGenerator).nextInt(20);
        randomCalls.verify(randomGenerator).nextInt(100);
        randomCalls.verify(randomGenerator).nextInt(5);
    }

    @Test
    void constructor_doesNotExposeNoArgRandomnessPath() {
        var publicConstructors = Arrays.asList(RandomPasswordGenerator.class.getConstructors());

        boolean hasNoArgConstructor =
                publicConstructors.stream().anyMatch(constructor -> constructor.getParameterCount() == 0);

        assertThat(hasNoArgConstructor).isFalse();
    }

    @Test
    void generator_isSpringManagedService() {
        Class<RandomPasswordGenerator> generatorType = RandomPasswordGenerator.class;

        boolean isSpringManagedService = generatorType.isAnnotationPresent(Service.class);

        assertThat(isSpringManagedService).isTrue();
    }
}
