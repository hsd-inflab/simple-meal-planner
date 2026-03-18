package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "services", importOptions = ImportOption.DoNotIncludeTests.class)
public class ServicesRulesTest {

    @ArchTest
    static final ArchRule services_only_access_models = classes()
            .that()
            .resideInAPackage("services..")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage("services..", "models..", "util..", "java..", "..jackson..");
}
