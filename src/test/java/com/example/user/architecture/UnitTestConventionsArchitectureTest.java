package com.example.user.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Garante que os testes unitários sigam as convenções do projeto.
 *
 * <p>Diferentemente das demais classes de arquitetura (que usam
 * {@link ImportOption.DoNotIncludeTests}), esta analisa <strong>apenas</strong> as
 * classes de teste, através do {@link OnlyIncludeTests} — o inverso daquela opção.</p>
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = UnitTestConventionsArchitectureTest.OnlyIncludeTests.class)
class UnitTestConventionsArchitectureTest {

    /** ImportOption que mantém somente as classes do source set de teste (inverso de {@link ImportOption.DoNotIncludeTests}). */
    static final class OnlyIncludeTests implements ImportOption {
        private static final ImportOption DO_NOT_INCLUDE_TESTS = new ImportOption.DoNotIncludeTests();

        @Override
        public boolean includes(Location location) {
            return !DO_NOT_INCLUDE_TESTS.includes(location);
        }
    }

    // Anotação de teste parametrizado verificada por nome para não acoplar o classpath ao módulo junit-params.
    private static final String PARAMETERIZED_TEST = "org.junit.jupiter.params.ParameterizedTest";

    @ArchTest
    static final ArchRule metodos_de_teste_so_existem_em_classes_de_teste = methods()
            .that().areAnnotatedWith(Test.class)
            .should().beDeclaredInClassesThat().haveSimpleNameEndingWith("Test")
            .allowEmptyShould(true)
            .as("Métodos anotados com @Test devem residir em classes cujo nome termina com 'Test'")
            .because("Os runners do Maven Surefire/Failsafe descobrem os testes pelo nome da classe; um @Test fora de uma classe '*Test' nunca é executado.");

    @ArchTest
    static final ArchRule metodos_de_teste_devem_ser_void = methods()
            .that().areAnnotatedWith(Test.class)
            .should().haveRawReturnType(void.class)
            .allowEmptyShould(true)
            .as("Métodos anotados com @Test devem ter retorno void")
            .because("O JUnit ignora o valor de retorno de um @Test; um retorno não-void indica uma asserção esquecida ou um método mal projetado.");

    @ArchTest
    static final ArchRule metodos_de_teste_nao_devem_ser_estaticos = methods()
            .that().areAnnotatedWith(Test.class)
            .should().notBeStatic()
            .allowEmptyShould(true)
            .as("Métodos anotados com @Test não devem ser estáticos")
            .because("O JUnit 5 instancia a classe de teste para cada método; um @Test estático não é executado como teste de instância.");

    @ArchTest
    static final ArchRule metodos_de_teste_nao_devem_ser_publicos = methods()
            .that().areAnnotatedWith(Test.class)
            .should().notBePublic()
            .allowEmptyShould(true)
            .as("Métodos anotados com @Test não devem ser public")
            .because("No JUnit 5 a visibilidade public é desnecessária; o padrão do projeto é manter métodos de teste package-private.");

    @ArchTest
    static final ArchRule classes_de_teste_nao_devem_ser_publicas =
            classes().that().haveSimpleNameEndingWith("Test")
                    .should().notBePublic()
                    .allowEmptyShould(true)
                    .as("Classes de teste ('*Test') não devem ser public")
                    .because("No JUnit 5 classes de teste não precisam ser public; o padrão do projeto é package-private.");

    @ArchTest
    static final ArchRule nao_devem_usar_junit_4 =
            noClasses().should().dependOnClassesThat().resideInAPackage("org.junit")
                    .as("Testes não devem usar JUnit 4 (pacote 'org.junit')")
                    .because("O projeto padroniza o JUnit 5 (Jupiter, 'org.junit.jupiter..'); misturar JUnit 4 fragmenta o ciclo de vida e a descoberta de testes.");

    /**
     * Verifica se a classe possui ao menos um método de teste ({@code @Test} ou {@code @ParameterizedTest}).
     */
    private static final ArchCondition<JavaClass> CONTER_AO_MENOS_UM_TESTE =
            new ArchCondition<JavaClass>("conter ao menos um método de teste (@Test ou @ParameterizedTest)") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    boolean possuiMetodoDeTeste = javaClass.getMethods().stream()
                            .anyMatch(UnitTestConventionsArchitectureTest::ehMetodoDeTeste);

                    if (!possuiMetodoDeTeste) {
                        events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("A classe '%s' termina com 'Test' mas não declara nenhum método de teste.",
                                        javaClass.getSimpleName())));
                    }
                }
            };

    private static boolean ehMetodoDeTeste(JavaMethod method) {
        return method.isAnnotatedWith(Test.class) || method.isAnnotatedWith(PARAMETERIZED_TEST);
    }

    @ArchTest
    static final ArchRule classes_de_teste_devem_conter_ao_menos_um_teste =
            classes().that().haveSimpleNameEndingWith("Test")
                    .and().areNotNestedClasses()
                    .and().resideOutsideOfPackage("..architecture..")
                    .should(CONTER_AO_MENOS_UM_TESTE)
                    .allowEmptyShould(true)
                    .as("Classes de teste unitário ('*Test') devem conter ao menos um teste (@Test ou @ParameterizedTest)")
                    .because("Uma classe '*Test' sem nenhum teste passa despercebida nos relatórios, dando uma falsa sensação de cobertura. Os testes de arquitetura (pacote '..architecture..') usam @ArchTest e ficam fora deste escopo.");

    // Predicado auxiliar reutilizável caso queira inverter regras no futuro.
    @SuppressWarnings("unused")
    private static final DescribedPredicate<JavaClass> SAO_CLASSES_DE_TESTE =
            DescribedPredicate.describe("são classes de teste",
                    javaClass -> javaClass.getSimpleName().endsWith("Test"));
}
