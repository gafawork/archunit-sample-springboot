package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida o padrão Repository e a camada de acesso a dados.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryArchitectureTest {

    @ArchTest
    static final ArchRule repositorios_sao_interfaces =
        classes().that().resideInAPackage("..domain.repository..")
            .should().beInterfaces()
            .as("Repositórios devem ser interfaces");

    @ArchTest
    static final ArchRule repositorios_sao_anotados_com_repository =
        classes().that().resideInAPackage("..domain.repository..")
            .should().beAnnotatedWith(Repository.class)
            .as("Repositórios devem ser anotados com @Repository");

    @ArchTest
    static final ArchRule repositorios_estendem_jpa_repository =
        classes().that().resideInAPackage("..domain.repository..")
            .should().beAssignableTo(JpaRepository.class)
            .as("Repositórios devem herdar de JpaRepository (operações CRUD padrão)");

    @ArchTest
    static final ArchRule repositorios_nao_dependem_de_dtos =
        noClasses().that().resideInAPackage("..domain.repository..")
            .should().dependOnClassesThat().resideInAPackage("..dto..")
            .as("Repositórios não devem depender de DTOs");

    @ArchTest
    static final ArchRule repositorios_nao_dependem_de_use_cases =
        noClasses().that().resideInAPackage("..domain.repository..")
            .should().dependOnClassesThat().resideInAPackage("..usecase..")
            .as("Repositórios não devem depender de use cases");

    @ArchTest
    static final ArchRule repositorios_seguem_padrao_de_nome =
        classes().that().resideInAPackage("..domain.repository..")
            .should().haveSimpleNameEndingWith("Repository")
            .as("Nomes de repositórios devem seguir o padrão [Entidade]Repository");
}
