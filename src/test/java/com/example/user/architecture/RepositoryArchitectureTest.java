package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
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

    @ArchTest
    static final ArchRule findall_em_repository_deve_ter_pageable =
        methods().that().haveNameMatching("findAll.*")
            .and().areDeclaredInClassesThat().resideInAPackage("..domain.repository..")
            .should(aceitarPageavel())
            .allowEmptyShould(true)
            .as("Métodos com findAll em repositórios devem aceitar Pageable - use paginação para grandes volumes de dados");

    @ArchTest
    static final ArchRule nao_devem_chamar_findall_sem_pageable =
        classes().that().resideInAPackage("..service..")
            .should(naoCharmarFindAllSemPaginacao())
            .allowEmptyShould(true)
            .as("Não devem chamar findAll sem Pageable - use paginação para grandes volumes de dados");

    private static ArchCondition<JavaMethod> aceitarPageavel() {
        return new ArchCondition<JavaMethod>("aceitar Pageable") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean temPageable = method.getRawParameterTypes().stream()
                    .anyMatch(param -> param.getName().equals("org.springframework.data.domain.Pageable"));
                
                if (!temPageable) {
                    String message = String.format(
                        "%s declara findAll sem Pageable - deve usar paginação para grandes volumes de dados",
                        method.getFullName()
                    );
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> naoCharmarFindAllSemPaginacao() {
        return new ArchCondition<JavaClass>("não chamar findAll sem paginação") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethodCallsFromSelf().stream()
                    .filter(call -> call.getTarget().getName().contains("findAll"))
                    .filter(call -> call.getTarget().getRawParameterTypes().size() == 0)
                    .forEach(call -> {
                        String message = String.format(
                            "%s chama %s() sem Pageable - deve usar paginação para grandes volumes de dados",
                            javaClass.getFullName(),
                            call.getTarget().getName()
                        );
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    });
            }
        };
    }
}
