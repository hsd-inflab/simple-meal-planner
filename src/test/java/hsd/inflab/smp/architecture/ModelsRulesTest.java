package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModelsRulesTest {

    @ArchTest
    static final ArchRule models_should_not_depend_on_services_or_frontend = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.model..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("hsd.inflab.smp.service..", "hsd.inflab.smp.frontend..");
}
