package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida o isolamento e as convenções da camada de domínio.
 *
 * Observação: diferentemente do que sugere o guia, neste projeto o domínio
 * usa anotações do Spring (@Service/@Repository) e JPA (@Entity). Portanto a
 * regra de isolamento aqui restringe apenas dependências às camadas superiores
 * (application/presentation), que é o ponto realmente relevante para o DDD.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainLayerArchitectureTest {

    @ArchTest
    static final ArchRule classes_de_dominio_residem_no_pacote_domain =
        classes().that().resideInAPackage("..domain..")
            .should().resideInAPackage("com.example.user.domain..")
            .as("Classes de domínio devem residir em ..domain..");

    @ArchTest
    static final ArchRule dominio_nao_depende_de_application =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..")
            .as("O domínio não deve depender da camada de aplicação");

    @ArchTest
    static final ArchRule dominio_nao_depende_de_presentation =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..presentation..")
            .as("O domínio não deve depender da camada de apresentação");

    @ArchTest
    static final ArchRule entidade_user_no_pacote_entity =
        classes().that().haveSimpleName("User")
            .should().resideInAPackage("..domain.entity..")
            .as("A entidade User deve residir em ..domain.entity..");

    @ArchTest
    static final ArchRule repositorios_terminam_com_repository =
        classes().that().resideInAPackage("..domain.repository..")
            .should().haveSimpleNameEndingWith("Repository")
            .as("Todos os repositórios devem ter o sufixo 'Repository'");

    @ArchTest
    static final ArchRule servicos_de_dominio_terminam_com_domain_service =
        classes().that().resideInAPackage("..domain.service..")
            .should().haveSimpleNameEndingWith("DomainService")
            .as("Todos os serviços de domínio devem ter o sufixo 'DomainService'");
}
