package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.stereotype.Service;

class RandomUsernameGeneratorTest {

    @Test
    void generateUsername_returnsAdjectiveNounAndTwoDigits() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        RandomUsernameGenerator generator = new RandomUsernameGenerator(randomGenerator);

        String username = generator.generateUsername();

        assertThat(username).matches("^[A-Z][a-z]+[A-Z][a-z]+\\d{2}$");
    }

    @Test
    void constructor_doesNotExposeNoArgRandomnessPath() {
        var publicConstructors = Arrays.asList(RandomUsernameGenerator.class.getConstructors());

        boolean hasNoArgConstructor =
                publicConstructors.stream().anyMatch(constructor -> constructor.getParameterCount() == 0);

        assertThat(hasNoArgConstructor).isFalse();
    }

    @Test
    void generateUsername_selectsAdjectiveNounAndDigitsThroughRandomProvider() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.nextInt(anyInt())).thenReturn(1, 2, 99);
        RandomUsernameGenerator generator = new RandomUsernameGenerator(randomGenerator);

        String username = generator.generateUsername();

        assertThat(username).isEqualTo("CalmFalcon99");
        InOrder randomCalls = inOrder(randomGenerator);
        randomCalls.verify(randomGenerator, times(2)).nextInt(10);
        randomCalls.verify(randomGenerator).nextInt(100);
    }

    @Test
    void generator_isSpringManagedService() {
        Class<RandomUsernameGenerator> generatorType = RandomUsernameGenerator.class;

        boolean isSpringManagedService = generatorType.isAnnotationPresent(Service.class);

        assertThat(isSpringManagedService).isTrue();
    }
}
