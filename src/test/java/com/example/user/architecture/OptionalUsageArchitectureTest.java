package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida o uso correto de {@link java.util.Optional} nas camadas de repositório e serviço.
 *
 * Regras aplicadas:
 * <ul>
 *     <li>Repositórios devem retornar {@code Optional} em buscas que recuperam uma única
 *         entidade (o {@code JpaRepository} já entrega {@code findById} retornando
 *         {@code Optional<T>}, portanto não é necessário redeclará-lo, apenas usá-lo).</li>
 *     <li>Serviços/Use Cases que consomem {@code Optional} devem tratá-lo
 *         (orElseThrow, orElse, orElseGet, map, filter, ifPresent, ifPresentOrElse).</li>
 *     <li>Nunca usar {@code Optional.get()} sem verificação prévia — equivale a usar {@code null}.</li>
 *     <li>{@code Optional} não deve ser usado em campos de entidade JPA, parâmetros de método
 *         ou coleções — apenas como retorno de métodos de serviço e repositório.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class OptionalUsageArchitectureTest {

    private static final String OPTIONAL = "java.util.Optional";

    /** Operações de tratamento aceitáveis ao consumir um {@code Optional}. */
    private static final Set<String> TRATAMENTOS_VALIDOS = Set.of(
        "orElseThrow", "orElse", "orElseGet", "map", "filter", "ifPresent", "ifPresentOrElse");

    @ArchTest
    static final ArchRule repositorios_devem_retornar_optional_em_buscas_unitarias =
        classes().that().resideInAPackage("..domain.repository..")
            .should(retornarOptionalEmBuscasUnitarias())
            .allowEmptyShould(true)
            .as("Repositórios devem retornar Optional em buscas que recuperam uma única entidade "
                + "(ex.: findByEmail). findById herdado de JpaRepository não precisa ser redeclarado");

    @ArchTest
    static final ArchRule use_cases_devem_tratar_optional =
        classes().that().resideInAPackage("..application.usecase..")
            .and().haveSimpleNameNotContaining("Test")
            .should(tratarOptionalAoConsumir())
            .allowEmptyShould(true)
            .as("Use cases que consomem Optional devem tratá-lo com orElseThrow, orElse, orElseGet, "
                + "map, filter, ifPresent ou ifPresentOrElse");

    @ArchTest
    static final ArchRule nenhuma_classe_deve_chamar_optional_get =
        noClasses().should().callMethod(Optional.class, "get")
            .as("Nunca use Optional.get() sem verificar antes — equivale a usar null. "
                + "Prefira orElseThrow ou orElseGet");

    @ArchTest
    static final ArchRule optional_nao_deve_ser_parametro_de_metodo =
        classes().should(naoReceberOptionalComoParametro())
            .allowEmptyShould(true)
            .as("Optional não deve ser usado como parâmetro de método ou construtor");

    @ArchTest
    static final ArchRule entidades_jpa_nao_devem_ter_campos_optional =
        classes().that().areAnnotatedWith(Entity.class)
            .should(naoPossuirCampoOptional())
            .allowEmptyShould(true)
            .as("Optional não deve ser usado em campos de entidade JPA");

    @ArchTest
    static final ArchRule optional_nao_deve_ser_usado_em_colecoes =
        classes().should(naoUsarOptionalDentroDeColecao())
            .allowEmptyShould(true)
            .as("Optional não deve ser usado dentro de coleções (ex.: List<Optional<T>>)");

    // ------------------------------------------------------------------------------------------
    // Condições customizadas
    // ------------------------------------------------------------------------------------------

    private static ArchCondition<JavaClass> retornarOptionalEmBuscasUnitarias() {
        return new ArchCondition<JavaClass>("retornar Optional em buscas unitárias") {
            @Override
            public void check(JavaClass repository, ConditionEvents events) {
                repository.getMethods().forEach(method -> {
                    String name = method.getName();
                    if (!name.startsWith("find") && !name.startsWith("get")) {
                        return;
                    }
                    JavaClass returnType = method.getRawReturnType();
                    if (retornaEntidadeUnicaSemOptional(returnType)) {
                        String message = String.format(
                            "%s retorna %s diretamente; buscas unitárias devem retornar Optional",
                            method.getFullName(), returnType.getSimpleName());
                        events.add(SimpleConditionEvent.violated(method, message));
                    }
                });
            }

            private boolean retornaEntidadeUnicaSemOptional(JavaClass returnType) {
                if (returnType.getName().equals(OPTIONAL)) {
                    return false;
                }
                if (returnType.isPrimitive() || returnType.getName().equals("void")) {
                    return false;
                }
                boolean isColecao = returnType.isAssignableTo(Collection.class)
                    || returnType.isAssignableTo(Map.class);
                return !isColecao;
            }
        };
    }

    private static ArchCondition<JavaClass> tratarOptionalAoConsumir() {
        return new ArchCondition<JavaClass>("tratar Optional ao consumir") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean consomeOptional = javaClass.getMethodCallsFromSelf().stream()
                    .anyMatch(call -> call.getTarget().getRawReturnType().getName().equals(OPTIONAL));

                if (!consomeOptional) {
                    return;
                }

                boolean trataOptional = javaClass.getMethodCallsFromSelf().stream()
                    .anyMatch(call -> call.getTargetOwner().getName().equals(OPTIONAL)
                        && TRATAMENTOS_VALIDOS.contains(call.getName()));

                if (!trataOptional) {
                    String message = String.format(
                        "%s consome Optional mas não o trata (orElseThrow, orElse, orElseGet, map, "
                            + "filter, ifPresent, ifPresentOrElse)", javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> naoReceberOptionalComoParametro() {
        return new ArchCondition<JavaClass>("não receber Optional como parâmetro") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaCodeUnit codeUnit : javaClass.getCodeUnits()) {
                    boolean temParametroOptional = codeUnit.getRawParameterTypes().stream()
                        .anyMatch(type -> type.getName().equals(OPTIONAL));
                    if (temParametroOptional) {
                        String message = String.format(
                            "%s recebe Optional como parâmetro", codeUnit.getFullName());
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> naoPossuirCampoOptional() {
        return new ArchCondition<JavaClass>("não possuir campo Optional") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getFields().stream()
                    .filter(field -> field.getRawType().getName().equals(OPTIONAL))
                    .forEach(field -> events.add(SimpleConditionEvent.violated(field,
                        String.format("%s é um campo Optional em entidade JPA", field.getFullName()))));
            }
        };
    }

    private static ArchCondition<JavaClass> naoUsarOptionalDentroDeColecao() {
        return new ArchCondition<JavaClass>("não usar Optional dentro de coleção") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getFields().forEach(field -> {
                    if (colecaoDeOptional(field.getType())) {
                        events.add(SimpleConditionEvent.violated(field,
                            String.format("%s é uma coleção de Optional", field.getFullName())));
                    }
                });
                javaClass.getMethods().forEach(method -> {
                    if (colecaoDeOptional(method.getReturnType())) {
                        events.add(SimpleConditionEvent.violated(method,
                            String.format("%s retorna uma coleção de Optional", method.getFullName())));
                    }
                });
            }

            private boolean colecaoDeOptional(JavaType type) {
                if (!(type instanceof JavaParameterizedType parameterizedType)) {
                    return false;
                }
                JavaClass raw = parameterizedType.toErasure();
                boolean isColecao = raw.isAssignableTo(Collection.class) || raw.isAssignableTo(Map.class);
                if (!isColecao) {
                    return false;
                }
                return parameterizedType.getActualTypeArguments().stream()
                    .anyMatch(arg -> arg.toErasure().getName().equals(OPTIONAL));
            }
        };
    }
}
