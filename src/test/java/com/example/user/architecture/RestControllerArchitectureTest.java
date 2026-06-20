package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Regras específicas de REST para os controllers.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class RestControllerArchitectureTest {

    @ArchTest
    static final ArchRule rest_controllers_tem_rest_controller_e_request_mapping =
        classes().that().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(RequestMapping.class)
            .as("REST controllers devem ter @RestController e @RequestMapping");

    @ArchTest
    static final ArchRule controllers_seguem_padrao_de_nome =
        classes().that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .as("Nomes de controllers devem seguir o padrão [Entidade]Controller");

    @ArchTest
    static final ArchRule controllers_nao_tem_metodos_estaticos =
        noMethods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .should().beStatic()
            .as("REST controllers não devem ter métodos estáticos");

    @ArchTest
    static final ArchRule metodos_publicos_de_controllers_retornam_response_entity =
        methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .and().arePublic()
            .should().haveRawReturnType("org.springframework.http.ResponseEntity")
            .as("Métodos públicos (endpoints) de controllers devem retornar ResponseEntity");
}
