package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Reforça convenções de código e estilo consistentes.
 */
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class CodingStyleArchitectureTest {

    @ArchTest
    static final ArchRule repositorios_anotados_com_repository =
        classes().that().resideInAPackage("..domain.repository..")
            .should().beAnnotatedWith(Repository.class)
            .as("Repositórios devem ser anotados com @Repository");

    @ArchTest
    static final ArchRule servicos_e_use_cases_anotados_com_service =
        classes().that().resideInAPackage("..domain.service..")
            .or().resideInAPackage("..application.usecase..")
            .should().beAnnotatedWith(Service.class)
            .as("Serviços de domínio e use cases devem ser anotados com @Service");

    @ArchTest
    static final ArchRule dtos_sao_publicos =
        classes().that().resideInAPackage("..application.dto..")
            .should().bePublic()
            .as("DTOs devem ser públicos");

    @ArchTest
    static final ArchRule classe_principal_no_pacote_raiz =
        classes().that().areAnnotatedWith(org.springframework.boot.autoconfigure.SpringBootApplication.class)
            .should().resideInAPackage("com.example")
            .as("A classe principal da aplicação deve residir no pacote raiz");

    @ArchTest
    static final ArchRule sem_injecao_por_campo =
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .as("Classes não devem usar injeção por campo (@Autowired em campos)");

    @ArchTest
    static final ArchRule sem_acesso_a_streams_padrao =
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .as("Classes não devem acessar System.out/System.err diretamente");

    @ArchTest
    static final ArchRule sem_lancamento_de_excecoes_genericas =
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
            .as("Classes não devem lançar exceções genéricas (Exception/RuntimeException/Throwable)");
}
