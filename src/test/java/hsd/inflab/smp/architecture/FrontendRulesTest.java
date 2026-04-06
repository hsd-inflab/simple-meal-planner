package hsd.inflab.smp.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "hsd.inflab.smp",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class FrontendRulesTest {

    @ArchTest
    static final ArchRule frontend_only_access_allowed_packages = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.frontend..")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                    "hsd.inflab.smp.frontend..",
                    "hsd.inflab.smp.service..",
                    "hsd.inflab.smp.model..",
                    "java..",
                    "javafx..",
                    "org.springframework.."
            );
}