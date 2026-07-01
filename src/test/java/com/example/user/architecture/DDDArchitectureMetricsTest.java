package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.LakosMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import com.tngtech.archunit.library.metrics.VisibilityMetrics;
import org.junit.jupiter.api.Test;
import com.tngtech.archunit.core.domain.JavaPackage;
import java.util.Set;

public class DDDArchitectureMetricsTest {

    @Test
    void calculateDddArchitectureMetrics() {
        // 1. Definição do pacote base e importação das classes
        // IMPORTANTE: Altere "com.seuprojeto" para o pacote raiz real da sua aplicação
        String basePackage = "com.example";
        JavaClasses classes = new ClassFileImporter().importPackages(basePackage);

        // 2. Extração dos pacotes para formar os componentes
        // Pegamos o pacote raiz e extraímos seus subpacotes diretos (ex: domain, application, infra)
        Set<JavaPackage> packages = classes.getPackage(basePackage).getSubpackages();
        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);

        // Definimos o pacote de domínio para extrair as métricas específicas dele
        String domainPackage = basePackage + ".user";

        System.out.println("=======================================================");
        System.out.println("MÉTRICAS ARQUITETURAIS DDD - " + basePackage);
        System.out.println("=======================================================\n");

        // ---------------------------------------------------------------------
        // 3. MÉTRICAS DE COMPONENTES (Robert C. Martin - Clean Architecture)
        // ---------------------------------------------------------------------
        ComponentDependencyMetrics martinMetrics = ArchitectureMetrics.componentDependencyMetrics(components);

        System.out.println("--- Component Dependency Metrics (Robert C. Martin) ---");
        System.out.println("Efferent Coupling (Ce) [Domain]                    : " + martinMetrics.getEfferentCoupling(domainPackage));
        System.out.println("Afferent Coupling (Ca) [Domain]                    : " + martinMetrics.getAfferentCoupling(domainPackage));
        System.out.println("Instability (I) [Domain]                           : " + martinMetrics.getInstability(domainPackage));
        System.out.println("Abstractness (A) [Domain]                          : " + martinMetrics.getAbstractness(domainPackage));
        System.out.println("Normalized Distance from Main Sequence (D) [Domain]: " + martinMetrics.getNormalizedDistanceFromMainSequence(domainPackage));

        // ---------------------------------------------------------------------
        // 4. MÉTRICAS DE LAKOS (John Lakos - Dependência Acumulada)
        // ---------------------------------------------------------------------
        LakosMetrics lakosMetrics = ArchitectureMetrics.lakosMetrics(components);

        System.out.println("\n--- Lakos Metrics (Análise de Acoplamento Global) ---");
        System.out.println("Cumulative Component Dependency (CCD)              : " + lakosMetrics.getCumulativeComponentDependency());
        System.out.println("Average Component Dependency (ACD)                 : " + lakosMetrics.getAverageComponentDependency());
        System.out.println("Relative Average Component Dependency (RACD)       : " + lakosMetrics.getRelativeAverageComponentDependency());
        System.out.println("Normalized Cumulative Component Dependency (NCCD)  : " + lakosMetrics.getNormalizedCumulativeComponentDependency());

        // ---------------------------------------------------------------------
        // 5. MÉTRICAS DE VISIBILIDADE (Herbert Dowalil)
        // ---------------------------------------------------------------------
        VisibilityMetrics visibilityMetrics = ArchitectureMetrics.visibilityMetrics(components);

        System.out.println("\n--- Visibility Metrics (Análise de Encapsulamento) ---");
        System.out.println("Relative Visibility [Domain]                       : " + visibilityMetrics.getRelativeVisibility(domainPackage));
        System.out.println("Average Relative Visibility                        : " + visibilityMetrics.getAverageRelativeVisibility());
        System.out.println("Global Relative Visibility                         : " + visibilityMetrics.getGlobalRelativeVisibility());
        System.out.println("=======================================================");
    }
}