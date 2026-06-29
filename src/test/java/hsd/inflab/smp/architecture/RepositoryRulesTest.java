package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class RepositoryRulesTest {
    @ArchTest
    static final ArchRule repository_may_only_access_entities = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.repository")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage("hsd.inflab.smp.entity", "hsd.inflab.smp.repository")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule repository_may_not_access_dtos = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.repository")
            .should()
            .accessClassesThat()
            .resideInAPackage("hsd.inflab.smp.dto..")
            .allowEmptyShould(true);
}
