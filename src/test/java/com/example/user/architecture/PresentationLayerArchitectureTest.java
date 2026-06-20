package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida a estrutura e as boas práticas da camada de apresentação (REST).
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class PresentationLayerArchitectureTest {

    @ArchTest
    static final ArchRule classes_de_apresentacao_no_pacote_presentation =
        classes().that().resideInAPackage("..presentation..")
            .should().resideInAPackage("com.example.user.presentation..")
            .as("Classes de apresentação devem residir em ..presentation..");

    @ArchTest
    static final ArchRule controllers_sao_anotados_com_rest_controller =
        classes().that().resideInAPackage("..presentation.controller..")
            .should().beAnnotatedWith(RestController.class)
            .as("Todos os controllers devem estar anotados com @RestController");

    @ArchTest
    static final ArchRule controllers_seguem_convencao_de_nomenclatura =
        classes().that().resideInAPackage("..presentation.controller..")
            .should().haveSimpleNameEndingWith("Controller")
            .as("Controllers devem seguir a convenção [Entidade]Controller");

    @ArchTest
    static final ArchRule controllers_nao_sao_anotados_com_service =
        noClasses().that().resideInAPackage("..presentation.controller..")
            .should().beAnnotatedWith(Service.class)
            .as("Controllers não devem ser anotados com @Service");

    @ArchTest
    static final ArchRule controllers_nao_dependem_de_servicos_de_dominio =
        noClasses().that().resideInAPackage("..presentation..")
            .should().dependOnClassesThat().resideInAPackage("..domain.service..")
            .as("Controllers não devem depender diretamente de serviços de domínio");

    @ArchTest
    static final ArchRule controllers_dependem_de_use_cases =
        classes().that().resideInAPackage("..presentation.controller..")
            .should().dependOnClassesThat().resideInAPackage("..application.usecase..")
            .as("Controllers devem depender de use cases");

    @ArchTest
    static final ArchRule exception_handlers_no_pacote_exception =
        classes().that().haveSimpleNameContaining("ExceptionHandler")
            .should().resideInAPackage("..presentation.exception..")
            .as("Tratadores de exceção devem residir em ..presentation.exception..");
}
