package com.example.user.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que a camada de aplicação (use cases) siga os padrões da arquitetura limpa.
 *
 * <p>Cada regra é verificada através de um check customizado ({@link ArchRuleCoverage}) que,
 * além de aplicar a regra, contabiliza quantos arquivos foram filtrados por ela e qual o
 * percentual de falhas — mantendo a falha do teste quando há violações.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class ApplicationLayerArchitectureTest2 {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLayerArchitectureTest2.class);

    // ---------------------------------------------------------------------------------------------
    // Predicados de "sujeito" (.that ...): definidos uma única vez e reutilizados tanto na regra
    // quanto na contagem de arquivos filtrados, garantindo que a métrica reflita exatamente o que
    // a regra seleciona.
    // ---------------------------------------------------------------------------------------------

    private static final DescribedPredicate<JavaClass> NO_PACOTE_APPLICATION =
        resideInAPackage("..application..");

    private static final DescribedPredicate<JavaClass> NOME_TERMINA_EM_USECASE =
        simpleNameEndingWith("UseCase");

    private static final DescribedPredicate<JavaClass> NO_PACOTE_USECASE =
        resideInAPackage("..application.usecase..");

    private static final DescribedPredicate<JavaClass> DTO_DE_TOPO =
        resideInAPackage("..application.dto..").and(TOP_LEVEL_CLASSES);

    // ---------------------------------------------------------------------------------------------
    // Regras
    // ---------------------------------------------------------------------------------------------

    private static final ArchRule REGRA_CLASSES_NO_PACOTE_APPLICATION =
        classes().that(NO_PACOTE_APPLICATION)
            .should().resideInAPackage("com.example.user.application..")
            .as("Classes da camada de aplicação devem residir em ..application..");

    private static final ArchRule REGRA_USE_CASES_NO_PACOTE_USECASE =
        classes().that(NOME_TERMINA_EM_USECASE)
            .should().resideInAPackage("..application.usecase..")
            .as("Use cases devem residir em ..application.usecase..");

    private static final ArchRule REGRA_USE_CASES_NOMENCLATURA =
        classes().that(NO_PACOTE_USECASE)
            .should().haveSimpleNameEndingWith("UseCase")
            .as("Use cases devem seguir a convenção [Operação]UseCase");

    private static final ArchRule REGRA_USE_CASES_ANOTADOS_SERVICE =
        classes().that(NO_PACOTE_USECASE)
            .should().beAnnotatedWith(Service.class)
            .as("Use cases devem ser anotados com @Service");

    private static final ArchRule REGRA_DTO_REQUEST_OU_RESPONSE =
        classes().that(DTO_DE_TOPO)
            .should().haveSimpleNameEndingWith("Request")
            .orShould().haveSimpleNameEndingWith("Response")
            .as("O pacote ..dto.. deve conter apenas DTOs de Request e Response");

    private static final ArchRule REGRA_APPLICATION_NAO_DEPENDE_DE_PRESENTATION =
        noClasses().that(NO_PACOTE_APPLICATION)
            .should().dependOnClassesThat().resideInAPackage("..presentation..")
            .as("A camada de aplicação não deve depender da camada de apresentação");

    private static final ArchRule REGRA_USE_CASES_DEPENDEM_DO_DOMINIO =
        classes().that(NO_PACOTE_USECASE)
            .should().dependOnClassesThat().resideInAPackage("..domain..")
            .as("Use cases devem depender de serviços de domínio ou repositórios");

    // ---------------------------------------------------------------------------------------------
    // Checks customizados (um @ArchTest por regra), cada um delegando para o coletor de métricas.
    // ---------------------------------------------------------------------------------------------

    @ArchTest
    static void classes_de_aplicacao_no_pacote_application(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_CLASSES_NO_PACOTE_APPLICATION, NO_PACOTE_APPLICATION, classes);
    }

    @ArchTest
    static void use_cases_no_pacote_usecase(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_USE_CASES_NO_PACOTE_USECASE, NOME_TERMINA_EM_USECASE, classes);
    }

    @ArchTest
    static void use_cases_seguem_convencao_de_nomenclatura(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_USE_CASES_NOMENCLATURA, NO_PACOTE_USECASE, classes);
    }

    @ArchTest
    static void use_cases_sao_anotados_com_service(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_USE_CASES_ANOTADOS_SERVICE, NO_PACOTE_USECASE, classes);
    }

    @ArchTest
    static void conteudo_do_pacote_dto_sao_request_ou_response(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_DTO_REQUEST_OU_RESPONSE, DTO_DE_TOPO, classes);
    }

    @ArchTest
    static void application_nao_depende_de_presentation(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_APPLICATION_NAO_DEPENDE_DE_PRESENTATION, NO_PACOTE_APPLICATION, classes);
    }

    @ArchTest
    static void use_cases_dependem_do_dominio(JavaClasses classes) {
        ArchRuleCoverage.check(REGRA_USE_CASES_DEPENDEM_DO_DOMINIO, NO_PACOTE_USECASE, classes);
    }

    /**
     * Conta quantas classes (arquivos) foram efetivamente importadas e verificadas durante a
     * checagem da arquitetura — o universo total sobre o qual todas as regras acima operam.
     */
    @ArchTest
    static void quantidade_de_arquivos_verificados(JavaClasses importedClasses) {
        int total = importedClasses.size();
        log.info("ArchUnit verificou {} arquivo(s)/classe(s) no pacote 'com.example.user'.", total);
        assertThat(total)
            .as("Nenhuma classe foi importada — verifique o pacote analisado em @AnalyzeClasses")
            .isPositive();
    }
}
