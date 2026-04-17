package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class ControllerRulesTest {

    @ArchTest
    static final ArchRule controller_may_only_access_service_dto_util_enum = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.controller")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                    "hsd.inflab.smp.service", "hsd.inflab.smp.dto", "hsd.inflab.smp.util", "hsd.inflab.smp.enums")
            .allowEmptyShould(true);

    // deliberate duplicate to strictly enforce using services
    @ArchTest
    static final ArchRule controller_may_not_access_repository_directly = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.controller")
            .should()
            .accessClassesThat()
            .resideInAnyPackage("hsd.inflab.smp.repository", "hsd.inflab.smp.entity")
            .allowEmptyShould(true);
}
