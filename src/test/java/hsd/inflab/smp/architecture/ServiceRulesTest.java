package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class ServiceRulesTest {

    @ArchTest
    static final ArchRule services_may_not_access_controllers = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.service..")
            .should()
            .accessClassesThat()
            .resideInAPackage("hsd.inflab.smp.controller");
}
