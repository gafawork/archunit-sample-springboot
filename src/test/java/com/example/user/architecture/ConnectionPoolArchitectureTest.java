package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida a configuração do pool de conexões do banco de dados.
 *
 * Verifica se:
 * - Existe apenas uma classe de configuração centralizada para banco de dados
 * - A configuração está localizada no pacote adequado
 * - A configuração está corretamente anotada com @Configuration
 * - Não há múltiplas DataSources configurados de forma descentralizada
 */
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class ConnectionPoolArchitectureTest {

    @ArchTest
    static final ArchRule configuracao_banco_dados_centralizada =
        classes().that().haveNameMatching(".*DataSource.*|.*DbConfig.*|.*DatabaseConfig.*")
            .and().haveNameNotMatching(".*Test.*")
            .should().resideInAPackage("com.example.config")
            .allowEmptyShould(true)
            .as("Configuração de DataSource deve estar centralizada no pacote de config");

    @ArchTest
    static final ArchRule configuracao_pool_conexoes_deve_ser_anotada =
        classes().that().resideInAPackage("com.example.config")
            .and().haveNameMatching(".*Config.*")
            .should().beAnnotatedWith("org.springframework.context.annotation.Configuration")
            .allowEmptyShould(true)
            .as("Classes de configuração devem ser anotadas com @Configuration");

    @ArchTest
    static final ArchRule repository_nao_deve_configurar_datasource =
        noClasses().that().resideInAPackage("..repository..")
            .should().dependOnClassesThat()
                .haveNameMatching(".*DataSource.*|javax\\.sql\\.DataSource|jakarta\\.sql\\.DataSource")
            .allowEmptyShould(true)
            .as("Repositórios não devem configurar ou ter dependência direta de DataSource");

    @ArchTest
    static final ArchRule configuracao_pool_nao_deve_ter_multiplos_datasources =
        classes().that().resideInAPackage("com.example.config")
            .and().haveNameMatching(".*Config.*")
            .should(possuirApenasUmDataSourceBean())
            .allowEmptyShould(true)
            .as("Deve haver apenas uma configuração centralizada de DataSource (pool de conexões)");

    private static ArchCondition<JavaClass> possuirApenasUmDataSourceBean() {
        return new ArchCondition<JavaClass>("conter apenas um Bean de DataSource") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long dataSourceBeans = javaClass.getMethods().stream()
                    .filter(method -> method.getAnnotations().stream()
                        .anyMatch(ann -> ann.getRawType().getName().equals("org.springframework.context.annotation.Bean")))
                    .filter(method -> method.getReturnType().getName().contains("DataSource"))
                    .count();

                if (dataSourceBeans > 1) {
                    String message = String.format(
                        "classe contém %d Beans de DataSource (esperado: máximo 1)",
                        dataSourceBeans
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
