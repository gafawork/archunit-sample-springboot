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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Relatorio completo das tres familias de metricas de arquitetura do ArchUnit.
 *
 * 1) LakosMetrics             -> CCD, ACD, RACD, NCCD          (John Lakos)
 * 2) ComponentDependencyMetrics -> Ca, Ce, I, A, D             (Robert C. Martin)
 * 3) VisibilityMetrics        -> RV, ARV, GRV                  (Herbert Dowalil)
 *
 * Conceito central: TODAS sao calculadas sobre "componentes", nao sobre classes
 * isoladas. Aqui cada componente = um pacote. As classes so entram como os
 * "elementos" de dentro de cada componente.
 *
 * Ca / Ce / I / A / D e RV sao POR COMPONENTE (recebem o identificador do pacote).
 * CCD / ACD / RACD / NCCD / ARV / GRV sao do SISTEMA INTEIRO (nao recebem argumento).
 */
public class RelatorioMetricas {

    private static final String PACOTE_RAIZ = "com.example";


    public static void main(String[] args) {

        // ------------------------------------------------------------------
        // PASSO 1 - importar as classes
        // ------------------------------------------------------------------
        JavaClasses classes = new ClassFileImporter().importPackages(PACOTE_RAIZ);

        // ------------------------------------------------------------------
        // PASSO 2 - definir os COMPONENTES
        //
        // getSubpackages()       -> apenas os subpacotes DIRETOS de com.exemplo
        // getSubpackagesInTree() -> toda a arvore abaixo de com.exemplo
        //
        // Essa escolha e a decisao mais importante da analise: ela define
        // qual e a granularidade de "componente" que voce esta medindo.
        // ------------------------------------------------------------------
        Set<JavaPackage> pacotes = classes.getPackage(PACOTE_RAIZ).getSubpackages();

        MetricsComponents<JavaClass> componentes = MetricsComponents.fromPackages(pacotes);

        List<String> ids = new ArrayList<>();
        for (MetricsComponent<JavaClass> componente : componentes) {
            ids.add(componente.getIdentifier());
        }
        ids.sort(Comparator.naturalOrder());

        System.out.println("Componentes analisados: " + ids.size());
        for (String id : ids) {
            System.out.println("  - " + id);
        }

        // ------------------------------------------------------------------
        // PASSO 3 - METRICAS DE LAKOS (sistema inteiro)
        // ------------------------------------------------------------------
        LakosMetrics lakos = ArchitectureMetrics.lakosMetrics(componentes);

        System.out.println();
        System.out.println("=== Lakos (sistema inteiro) ===");
        System.out.printf("CCD  = %d%n", lakos.getCumulativeComponentDependency());
        System.out.printf("ACD  = %.4f%n", lakos.getAverageComponentDependency());
        System.out.printf("RACD = %.4f   (referencia: <= 0.6 para grafo aciclico com n >= 5)%n",
                lakos.getRelativeAverageComponentDependency());
        System.out.printf("NCCD = %.4f   (referencia: ~0.85 a 1.10 saudavel; > 2.0 provavel ciclo)%n",
                lakos.getNormalizedCumulativeComponentDependency());

        // ------------------------------------------------------------------
        // PASSO 4 - METRICAS DE MARTIN (por componente)
        // ------------------------------------------------------------------
        ComponentDependencyMetrics martin = ArchitectureMetrics.componentDependencyMetrics(componentes);

        System.out.println();
        System.out.println("=== Robert C. Martin (por componente) ===");
        System.out.printf("%-32s %5s %5s %8s %8s %8s%n", "COMPONENTE", "Ca", "Ce", "I", "A", "D");
        for (String id : ids) {
            System.out.printf("%-32s %5d %5d %8.3f %8.3f %8.3f%n",
                    id,
                    martin.getAfferentCoupling(id),
                    martin.getEfferentCoupling(id),
                    martin.getInstability(id),
                    martin.getAbstractness(id),
                    martin.getNormalizedDistanceFromMainSequence(id));
        }

        // ------------------------------------------------------------------
        // PASSO 5 - METRICAS DE VISIBILIDADE / DOWALIL
        //
        // RV  -> por componente
        // ARV -> media simples dos RVs (cada pacote pesa igual)
        // GRV -> razao global (soma visiveis / soma total; pacote grande pesa mais)
        // ------------------------------------------------------------------
        VisibilityMetrics visibilidade = ArchitectureMetrics.visibilityMetrics(componentes);

        System.out.println();
        System.out.println("=== Herbert Dowalil - Visibilidade ===");
        System.out.printf("%-32s %8s%n", "COMPONENTE", "RV");
        for (String id : ids) {
            System.out.printf("%-32s %8.3f%n", id, visibilidade.getRelativeVisibility(id));
        }
        System.out.println();
        System.out.printf("ARV = %.4f   (media dos RV - todo pacote pesa igual)%n",
                visibilidade.getAverageRelativeVisibility());
        System.out.printf("GRV = %.4f   (global - todas as classes de todos os pacotes juntas)%n",
                visibilidade.getGlobalRelativeVisibility());

        System.out.println();
        System.out.println("Quanto menor RV/ARV/GRV, melhor o encapsulamento (Information Hiding).");
    }
}