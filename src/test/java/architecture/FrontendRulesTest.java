package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "frontend", importOptions = ImportOption.DoNotIncludeTests.class)
public class FrontendRulesTest {

    @ArchTest
    static final ArchRule frontend_only_access_services = classes()
            .that()
            .resideInAPackage("frontend..")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage("frontend..", "services..", "java..", "javafx..", "models..");
    // hier muss model raus um Architekturabhängigkeit zu verbessern
}
