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

/**
 * Valida o uso resiliente de clientes HTTP declarativos (OpenFeign).
 *
 * Verifica se:
 * - Todo código anotado com @FeignClient utiliza Retry (Resilience4j @Retry
 *   ou Spring Retry @Retryable), na própria interface ou em seus métodos.
 */
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class FeignClientArchitectureTest {

    private static final String FEIGN_CLIENT =
        "org.springframework.cloud.openfeign.FeignClient";

    private static final String RESILIENCE4J_RETRY =
        "io.github.resilience4j.retry.annotation.Retry";

    private static final String SPRING_RETRYABLE =
        "org.springframework.retry.annotation.Retryable";

    @ArchTest
    static final ArchRule feign_clients_devem_utilizar_retry =
        classes().that().areAnnotatedWith(FEIGN_CLIENT)
            .should(utilizarRetry())
            .allowEmptyShould(true)
            .as("Todo código anotado com @FeignClient deve utilizar Retry "
                + "(@Retry do Resilience4j ou @Retryable do Spring Retry)");

    private static ArchCondition<JavaClass> utilizarRetry() {
        return new ArchCondition<JavaClass>("utilizar @Retry ou @Retryable") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean retryNaClasse = possuiAnotacaoRetry(javaClass);

                boolean retryEmAlgumMetodo = javaClass.getMethods().stream()
                    .anyMatch(method -> method.getAnnotations().stream()
                        .anyMatch(ann -> isRetry(ann.getRawType().getName())));

                if (!retryNaClasse && !retryEmAlgumMetodo) {
                    String message = String.format(
                        "%s está anotado com @FeignClient mas não utiliza Retry "
                            + "(@Retry ou @Retryable) na interface nem em seus métodos",
                        javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }

            private boolean possuiAnotacaoRetry(JavaClass javaClass) {
                return javaClass.getAnnotations().stream()
                    .anyMatch(ann -> isRetry(ann.getRawType().getName()));
            }

            private boolean isRetry(String annotationName) {
                return RESILIENCE4J_RETRY.equals(annotationName)
                    || SPRING_RETRYABLE.equals(annotationName);
            }
        };
    }
}
