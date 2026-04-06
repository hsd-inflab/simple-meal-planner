package hsd.inflab.smp.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class ServicesRulesTest {

    @ArchTest
    static final ArchRule services_only_access_models = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.service..")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                    "hsd.inflab.smp.service..",
                    "hsd.inflab.smp.model..",
                    "java..",
                    "org.springframework..",
                    "hsd.inflab.smp.util..",
                    "..jackson.."
            );
}