package com.example.user.architecture;


import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.LakosMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponent;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import com.tngtech.archunit.library.metrics.VisibilityMetrics;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mesmas metricas, agora como teste de arquitetura que quebra o build
 * quando os limites sao ultrapassados. Este e o uso tipico no dia a dia:
 * voce mede uma vez, congela o valor atual como limite, e impede que piore.
 */
class MetricasArquiteturaTest {

    private static final String PACOTE_RAIZ = "com.example";


    private MetricsComponents<JavaClass> componentes() {
        JavaClasses classes = new ClassFileImporter().importPackages(PACOTE_RAIZ);
        Set<JavaPackage> pacotes = classes.getPackage(PACOTE_RAIZ).getSubpackages();
        return MetricsComponents.fromPackages(pacotes);
    }

    @Test
    void acoplamentoDeLakosDentroDoLimite() {
        LakosMetrics metricas = ArchitectureMetrics.lakosMetrics(componentes());

        // RACD: para grafo aciclico nao trivial (n >= 5) o proprio ArchUnit
        // documenta que o valor fica limitado a 0.6. Acima disso indica ciclo.
        assertTrue(metricas.getRelativeAverageComponentDependency() <= 0.6d,
                "RACD acima de 0.6 - provavel ciclo de dependencia entre pacotes: "
                        + metricas.getRelativeAverageComponentDependency());

        // NCCD: acima de 2.0 e considerado sinal forte de ciclos / acoplamento excessivo.
        assertTrue(metricas.getNormalizedCumulativeComponentDependency() < 2.0d,
                "NCCD acima de 2.0 - arquitetura excessivamente acoplada: "
                        + metricas.getNormalizedCumulativeComponentDependency());
    }

    @Test
    void nenhumComponenteLongeDaSequenciaPrincipal() {
        MetricsComponents<JavaClass> componentes = componentes();
        ComponentDependencyMetrics metricas = ArchitectureMetrics.componentDependencyMetrics(componentes);

        for (MetricsComponent<JavaClass> componente : componentes) {
            String id = componente.getIdentifier();
            double d = metricas.getNormalizedDistanceFromMainSequence(id);

            // D = | A + I - 1 |. Limite de alerta usado na pratica: 0.7
            assertTrue(d <= 0.7d, "Componente " + id + " esta longe da sequencia principal (D=" + d + ")");
        }
    }

    @Test
    void encapsulamentoAceitavel() {
        MetricsComponents<JavaClass> componentes = componentes();
        VisibilityMetrics metricas = ArchitectureMetrics.visibilityMetrics(componentes);

        // Nao existe faixa oficial publicada por Dowalil.
        // A pratica e: medir o valor atual e usar como teto para nao regredir.
        assertTrue(metricas.getGlobalRelativeVisibility() <= 1.0d,
                "GRV regrediu: " + metricas.getGlobalRelativeVisibility());

        assertTrue(metricas.getAverageRelativeVisibility() <= 1.0d,
                "ARV regrediu: " + metricas.getAverageRelativeVisibility());

        for (MetricsComponent<JavaClass> componente : componentes) {
            String id = componente.getIdentifier();
            double rv = metricas.getRelativeVisibility(id);
            assertTrue(rv <= 1.0d, "RV do componente " + id + " regrediu: " + rv);
        }
    }
}
