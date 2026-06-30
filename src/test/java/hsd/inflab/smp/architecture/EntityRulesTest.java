package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class EntityRulesTest {
    @ArchTest
    static final ArchRule entity_may_not_access_businesslogic = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.entity")
            .should()
            .accessClassesThat()
            .resideInAnyPackage(
                    "hsd.inflab.smp.service",
                    "hsd.inflab.smp.repository",
                    "hsd.inflab.smp.controller",
                    "hsd.inflab.smp.dto..",
                    "hsd.inflab.smp.util");
}
