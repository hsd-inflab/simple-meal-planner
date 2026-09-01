package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Set;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class ServiceRulesTest {

    private static final Set<String> UNSCOPED_REPOSITORY_METHODS = Set.of("findAll", "findById", "deleteById");

    private static final DescribedPredicate<JavaMethodCall> CALL_UNSCOPED_REPOSITORY_METHOD =
            new DescribedPredicate<>("call an unscoped repository method") {
                @Override
                public boolean test(JavaMethodCall methodCall) {
                    return methodCall.getTargetOwner().getPackageName().equals("hsd.inflab.smp.repository")
                            && UNSCOPED_REPOSITORY_METHODS.contains(methodCall.getName());
                }
            };

    @ArchTest
    static final ArchRule services_may_not_access_controllers = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.service..")
            .should()
            .accessClassesThat()
            .resideInAPackage("hsd.inflab.smp.controller");

    @ArchTest
    static final ArchRule services_may_not_call_unscoped_repository_methods = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.service..")
            .should()
            .callMethodWhere(CALL_UNSCOPED_REPOSITORY_METHOD);
}
