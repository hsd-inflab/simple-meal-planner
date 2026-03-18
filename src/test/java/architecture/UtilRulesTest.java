package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "util", importOptions = ImportOption.DoNotIncludeTests.class)
public class UtilRulesTest {

    @ArchTest
    static final ArchRule util_may_be_accessed_by_everyone = classes()
            .that()
            .resideInAPackage("util..")
            .should()
            .onlyBeAccessed()
            .byAnyPackage("frontend..", "services..", "models..", "util..");
}
