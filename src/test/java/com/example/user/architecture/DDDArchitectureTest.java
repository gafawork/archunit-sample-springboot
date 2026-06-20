package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Valida os princípios de Domain-Driven Design entre as camadas.
 *
 * Fluxo de dependência correto (apenas para dentro):
 *   Presentation -> Application -> Domain
 */
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class DDDArchitectureTest {

    @ArchTest
    static final ArchRule camadas_devem_respeitar_a_arquitetura_em_camadas =
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Presentation").definedBy("..presentation..")
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Presentation")
            .as("As camadas devem respeitar o fluxo de dependência do DDD");

    @ArchTest
    static final ArchRule dominio_nao_depende_de_application_nem_presentation =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..presentation..")
            .as("A camada de domínio não deve depender das camadas de aplicação ou apresentação");

    @ArchTest
    static final ArchRule application_nao_depende_de_presentation =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..presentation..")
            .as("A camada de aplicação não deve depender da camada de apresentação");

    @ArchTest
    static final ArchRule entidades_devem_estar_anotadas_com_entity =
        classes().that().resideInAPackage("..domain.entity..")
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(Entity.class)
            .as("Classes de entidade devem estar anotadas com @Entity");

    @ArchTest
    static final ArchRule todas_as_classes_de_negocio_residem_no_pacote_user =
        classes().that().resideInAPackage("..user..")
            .should().resideInAPackage("com.example.user..")
            .as("Todas as classes de negócio devem residir em com.example.user..");

    @ArchTest
    static final ArchRule arquitetura_livre_de_ciclos =
        slices().matching("com.example.user.(*)..")
            .should().beFreeOfCycles()
            .as("As camadas não devem formar dependências cíclicas");
}
