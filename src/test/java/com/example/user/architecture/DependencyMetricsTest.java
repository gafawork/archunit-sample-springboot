package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponent;
import com.tngtech.archunit.library.metrics.MetricsComponents;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Versão corrigida de DependencyMetricsTest.
 *
 * Mudanças em relação ao original:
 *  1. @AnalyzeClasses -> importa UMA vez e usa o cache do archunit-junit5
 *     (o original refazia o import do classpath inteiro em cada @Test).
 *  2. DoNotIncludeTests -> as próprias classes de teste não entram nas métricas.
 *  3. Componentes montados a partir de MODULES_ROOT, não de "com.example":
 *     se "com.example" tiver um único subpacote, N=1 e todas as métricas
 *     viram constantes (CCD=ACD=RACD=NCCD=1,0; Ce=Ca=0; I=1 por valor de
 *     reposição do ArchUnit) — números plausíveis que não medem nada.
 *  4. Itera TODOS os componentes em vez de findFirst() sobre um Set sem ordem.
 *  5. ARV/GRV fora do laço (são métricas globais, não por componente).
 *  6. Asserts de baseline: sem eles o teste é um relatório que nunca falha.
 */
@AnalyzeClasses(
        packages = DependencyMetricsTest.ROOT,
        importOptions = ImportOption.DoNotIncludeTests.class)
class DependencyMetricsTest {

    static final String ROOT = "com.example";

    /**
     * Pacote pai cujos subpacotes de 1º nível são os seus módulos.
     * AJUSTE para onde os módulos realmente vivem. Confira com o teste
     * granularidade_dos_componentes() abaixo antes de confiar nos números.
     */
    static final String MODULES_ROOT = ROOT + ".user";

    private static MetricsComponents<JavaClass> componentsOf(JavaClasses classes) {
        Set<JavaPackage> modulePackages = classes.getPackage(MODULES_ROOT).getSubpackages();
        return MetricsComponents.fromPackages(modulePackages);
    }

    /** Trava de sanidade: com poucos componentes as métricas não significam nada. */
    @ArchTest
    void granularidade_dos_componentes(JavaClasses classes) {
        var components = componentsOf(classes);
        components.forEach(c -> System.out.println("componente: " + c.getIdentifier()));

        assertThat(components)
                .as("componentes derivados de %s — com menos de 3 as métricas são degeneradas", MODULES_ROOT)
                .hasSizeGreaterThanOrEqualTo(3);
    }

    @ArchTest
    void cumulativeDependencyMetrics(JavaClasses classes) {
        var metrics = ArchitectureMetrics.lakosMetrics(componentsOf(classes));

        System.out.println("Cumulative Component Dependency: " + metrics.getCumulativeComponentDependency());
        System.out.println("Average Component Dependency: " + metrics.getAverageComponentDependency());
        System.out.println("Relative Average Component Dependency: " + metrics.getRelativeAverageComponentDependency());
        System.out.println("Normalized Cumulative Component Dependency: " + metrics.getNormalizedCumulativeComponentDependency());

        assertThat(metrics.getNormalizedCumulativeComponentDependency())
                .as("NCCD")
                .isLessThan(1.5); // troque pelo seu valor atual e só permita baixar
    }

    @ArchTest
    void componentDependencyMetrics(JavaClasses classes) {
        var components = componentsOf(classes);
        var metrics = ArchitectureMetrics.componentDependencyMetrics(components);

        for (MetricsComponent<JavaClass> component : components) {
            var id = component.getIdentifier();

            System.out.printf("%-40s Ce=%-3d Ca=%-3d I=%.2f A=%.2f D=%.2f%n",
                    id,
                    metrics.getEfferentCoupling(id),
                    metrics.getAfferentCoupling(id),
                    metrics.getInstability(id),
                    metrics.getAbstractness(id),
                    metrics.getNormalizedDistanceFromMainSequence(id));

            assertThat(metrics.getNormalizedDistanceFromMainSequence(id))
                    .as("D de %s", id)
                    .isLessThanOrEqualTo(0.5);
        }
    }

    @ArchTest
    void visibilityMetrics(JavaClasses classes) {
        var components = componentsOf(classes);
        var metrics = ArchitectureMetrics.visibilityMetrics(components);

        for (MetricsComponent<JavaClass> component : components) {
            var id = component.getIdentifier();
            System.out.printf("%-40s RV=%.2f%n", id, metrics.getRelativeVisibility(id));
        }

        // Globais: ficam FORA do laço.
        System.out.println("Average Relative Visibility: " + metrics.getAverageRelativeVisibility());
        System.out.println("Global Relative Visibility: " + metrics.getGlobalRelativeVisibility());

        assertThat(metrics.getAverageRelativeVisibility())
                .as("ARV")
                .isLessThan(0.6);
    }
}