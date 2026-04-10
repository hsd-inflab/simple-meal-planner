package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class UtilRulesTest {

    @ArchTest
    static final ArchRule util_may_be_accessed_by_everyone = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.util..")
            .should()
            .onlyBeAccessed()
            .byAnyPackage(
                    "hsd.inflab.smp.frontend..",
                    "hsd.inflab.smp.service..",
                    "hsd.inflab.smp.model..",
                    "hsd.inflab.smp.util..");
}
