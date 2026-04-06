import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "SimpleMealPlannerSpringbootDemo/model", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModelsRulesTest {

    @ArchTest
    static final ArchRule models_should_not_depend_on_services_or_frontend = noClasses()
            .that()
            .resideInAPackage("models..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("services..", "frontend..");
}
