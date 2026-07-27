package com.example.user.architecture;



import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.LakosMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponent;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import com.tngtech.archunit.library.metrics.VisibilityMetrics;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Métricas de arquitetura (Lakos / Martin / Dowalil) com ArchUnit >= 0.22.
 *
 * REGRA DE OURO — são DUAS decisões independentes:
 *
 *  1) O QUE IMPORTAR  -> sempre o PACOTE RAIZ da aplicação.
 *     O grafo de dependências precisa estar completo; o que não é importado
 *     simplesmente não existe para o cálculo e as métricas ficam otimistas.
 *
 *  2) O QUE É UM "COMPONENTE" -> o PACOTE DE MÓDULO (+ toda a sua árvore de
 *     subpacotes). Nunca a raiz (1 componente = métrica degenerada) e nunca
 *     cada pacote-folha (infla N, e a visibilidade vira ruído porque em Java
 *     classes precisam ser public só para atravessar subpacotes do módulo).
 */
@AnalyzeClasses(
        packages = ArchitectureMetricsTest.ROOT,
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
class ArchitectureMetricsTest {

    static final String ROOT = "com.exmple";

    /** Pacote pai sob o qual cada subpacote de 1º nível é um módulo. */
    static final String MODULES_ROOT = ROOT + ".modules";

    // ------------------------------------------------------------------
    // Definição dos componentes
    // ------------------------------------------------------------------

    /**
     * Opção A (recomendada): módulos = subpacotes de 1º nível de MODULES_ROOT.
     *
     * fromPackages() já inclui "all classes contained in the respective package
     * or any subpackage" — ou seja, os subpacotes internos (domain, application,
     * infrastructure, dto, mapper...) entram no MESMO componente. É exatamente
     * o que se quer: o componente é a FRONTEIRA do módulo.
     *
     * IMPORTANTE: nunca passe um pacote e um subpacote dele na mesma coleção —
     * a mesma classe cairia em dois componentes e as dependências seriam
     * contadas em dobro.
     */
    private static MetricsComponents<JavaClass> modules(JavaClasses classes) {
        JavaPackage modulesRoot = classes.getPackage(MODULES_ROOT);
        Set<JavaPackage> modulePackages = modulesRoot.getSubpackages();
        return MetricsComponents.fromPackages(modulePackages);
    }

    /**
     * Opção B: módulos em níveis/prefixos irregulares (ou multi-módulo Maven).
     * Aqui você mesmo decide o identificador do componente de cada classe.
     * Classes que não casam com nenhum módulo devem ser filtradas ANTES,
     * senão viram um componente "lixo" que distorce ACD/NCCD.
     */
    @SuppressWarnings("unused")
    private static MetricsComponents<JavaClass> modulesByPrefix(JavaClasses classes) {
        Set<JavaClass> relevant = new LinkedHashSet<>();
        for (JavaClass clazz : classes) {
            if (clazz.getPackageName().startsWith(MODULES_ROOT + ".")) {
                relevant.add(clazz);
            }
        }
        return MetricsComponents.from(relevant, ArchitectureMetricsTest::moduleIdentifierOf);
    }

    private static String moduleIdentifierOf(JavaClass clazz) {
        String rest = clazz.getPackageName().substring(MODULES_ROOT.length() + 1);
        int dot = rest.indexOf('.');
        return MODULES_ROOT + "." + (dot < 0 ? rest : rest.substring(0, dot));
    }

    // ------------------------------------------------------------------
    // 1. Lakos — complexidade acumulada do sistema (métrica GLOBAL)
    // ------------------------------------------------------------------

    /**
     * Ciclos explodem o CCD. Rode esta regra ANTES de olhar para o NCCD,
     * senão você fica caçando um número sem entender a causa.
     */
    @ArchTest
    static final ArchRule modulos_sem_ciclos = SlicesRuleDefinition
            .slices()
            .matching(MODULES_ROOT + ".(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    void lakos_dentro_do_limite(JavaClasses classes) {
        LakosMetrics metrics = ArchitectureMetrics.lakosMetrics(modules(classes));

        System.out.printf("CCD=%d  ACD=%.2f  RACD=%.2f  NCCD=%.2f%n",
                metrics.getCumulativeComponentDependency(),
                metrics.getAverageComponentDependency(),
                metrics.getRelativeAverageComponentDependency(),
                metrics.getNormalizedCumulativeComponentDependency());

        // NCCD ~1.0 = tão acoplado quanto uma árvore binária balanceada.
        // < 1.0 = estrutura mais "achatada"/desacoplada. > 1.5 = investigar.
        // Ajuste o teto ao seu baseline atual e só permita que ele DIMINUA.
        assertThat(metrics.getNormalizedCumulativeComponentDependency())
                .as("NCCD dos módulos")
                .isLessThan(1.5);
    }

    // ------------------------------------------------------------------
    // 2. Martin — acoplamento POR componente
    // ------------------------------------------------------------------

    @ArchTest
    void distancia_da_sequencia_principal(JavaClasses classes) {
        MetricsComponents<JavaClass> components = modules(classes);
        ComponentDependencyMetrics metrics = ArchitectureMetrics.componentDependencyMetrics(components);

        for (MetricsComponent<JavaClass> component : components) {
            String id = component.getIdentifier();

            System.out.printf("%-45s Ce=%-3d Ca=%-3d I=%.2f A=%.2f D=%.2f%n",
                    id,
                    metrics.getEfferentCoupling(id),
                    metrics.getAfferentCoupling(id),
                    metrics.getInstability(id),
                    metrics.getAbstractness(id),
                    metrics.getNormalizedDistanceFromMainSequence(id));

            // Zona de dor (concreto + estável) e zona de inutilidade (abstrato + instável).
            assertThat(metrics.getNormalizedDistanceFromMainSequence(id))
                    .as("D do módulo %s", id)
                    .isLessThanOrEqualTo(0.5);
        }
    }

    // ------------------------------------------------------------------
    // 3. Dowalil — visibilidade / encapsulamento
    // ------------------------------------------------------------------

    /**
     * RV = classes public / total de classes do componente.
     * Esta é a métrica que MAIS depende da granularidade: só faz sentido se o
     * componente for o módulo inteiro (com subpacotes dentro). Medida em
     * pacote-folha, tudo parece "exposto" e o número é inútil.
     */
    @ArchTest
    void visibilidade_dos_modulos(JavaClasses classes) {
        MetricsComponents<JavaClass> components = modules(classes);
        VisibilityMetrics metrics = ArchitectureMetrics.visibilityMetrics(components);

        for (MetricsComponent<JavaClass> component : components) {
            String id = component.getIdentifier();
            System.out.printf("%-45s RV=%.2f%n", id, metrics.getRelativeVisibility(id));
        }

        System.out.printf("ARV=%.2f  GRV=%.2f%n",
                metrics.getAverageRelativeVisibility(),
                metrics.getGlobalRelativeVisibility());

        // Quanto menor, melhor o information hiding. Comece pelo seu valor atual
        // como baseline (ex.: 0.85) e vá apertando a cada refatoração.
        assertThat(metrics.getAverageRelativeVisibility())
                .as("ARV dos módulos")
                .isLessThan(0.6);
    }
}