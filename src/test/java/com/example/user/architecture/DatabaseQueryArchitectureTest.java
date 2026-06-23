package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Valida as práticas de queries ao banco de dados.
 *
 * Verifica se as queries não utilizam SELECT * (trazendo todos os campos)
 * através da validação de anotações @Query em repositórios.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class DatabaseQueryArchitectureTest {

    @ArchTest
    static final ArchRule repositorio_queries_nao_devem_usar_select_asterisco =
        methods().that().areAnnotatedWith("org.springframework.data.jpa.repository.Query")
            .and().areDeclaredInClassesThat().resideInAPackage("..repository..")
            .should(naoUtilizarSelectAsterisco())
            .allowEmptyShould(true)
            .as("Queries em repositórios não devem usar SELECT * (devem listar campos específicos)");

    private static ArchCondition<JavaMethod> naoUtilizarSelectAsterisco() {
        return new ArchCondition<JavaMethod>("não utilizar SELECT *") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getAnnotations().stream()
                    .filter(ann -> ann.getRawType().getName().equals("org.springframework.data.jpa.repository.Query"))
                    .forEach(queryAnnotation -> {
                        Object value = queryAnnotation.get("value");
                        if (value instanceof String) {
                            String query = ((String) value).toUpperCase().trim();
                            if (query.contains("SELECT *")) {
                                String message = String.format(
                                    "método %s contém SELECT * na query",
                                    method.getFullName()
                                );
                                events.add(SimpleConditionEvent.violated(method, message));
                            }
                        }
                    });
            }
        };
    }
}
