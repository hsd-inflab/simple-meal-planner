package hsd.inflab.smp.service.instantiation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hsd.inflab.smp.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = PasswordServiceInstantiationTest.TestContext.class)
class PasswordServiceInstantiationTest {

    @Autowired
    private PasswordService passwordService;

    @Test
    void passwordService_isInstantiated() {
        assertNotNull(passwordService);
    }

    @SpringBootConfiguration
    @Import(PasswordService.class)
    static class TestContext {}
}
