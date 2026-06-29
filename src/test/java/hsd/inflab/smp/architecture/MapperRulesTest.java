package hsd.inflab.smp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hsd.inflab.smp", importOptions = ImportOption.DoNotIncludeTests.class)
public class MapperRulesTest {

    @ArchTest
    static final ArchRule mapper_may_only_access_dto_entity_enum_util = classes()
            .that()
            .resideInAPackage("hsd.inflab.smp.mapper..")
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                    "hsd.inflab.smp.mapper..",
                    "hsd.inflab.smp.dto..",
                    "hsd.inflab.smp.entity..",
                    "hsd.inflab.smp.enums..",
                    "hsd.inflab.smp.util..",
                    "java..",
                    "jakarta..",
                    "org.mapstruct..",
                    "org.springframework..")
            .allowEmptyShould(true);

    // Mapper dürfen keine Repositories oder Controller berühren (kein DB-/Web-Zugriff im Mapping-Layer)
    @ArchTest
    static final ArchRule mapper_may_not_access_repository_or_controller = noClasses()
            .that()
            .resideInAPackage("hsd.inflab.smp.mapper..")
            .should()
            .accessClassesThat()
            .resideInAnyPackage("hsd.inflab.smp.repository..", "hsd.inflab.smp.controller..")
            .allowEmptyShould(true);
}
