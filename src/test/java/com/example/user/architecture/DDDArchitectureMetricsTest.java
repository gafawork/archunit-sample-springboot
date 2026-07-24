package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.LakosMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import com.tngtech.archunit.library.metrics.VisibilityMetrics;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import com.tngtech.archunit.core.domain.JavaPackage;
import java.util.Set;

@Epic("Arquitetura")
@Feature("Métricas DDD")
public class DDDArchitectureMetricsTest {

    @Test
    @Story("Cálculo de métricas de acoplamento e visibilidade do domínio")
    void calculateDddArchitectureMetrics() {
        // 1. Definição do pacote base e importação das classes
        // IMPORTANTE: Altere "com.seuprojeto" para o pacote raiz real da sua aplicação
        String basePackage = "com.example";
        JavaClasses classes = importClasses(basePackage);

        // 2. Extração dos pacotes para formar os componentes
        // Pegamos o pacote raiz e extraímos seus subpacotes diretos (ex: domain, application, infra)
        Set<JavaPackage> packages = classes.getPackage(basePackage).getSubpackages();
        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);

        // Definimos o pacote de domínio para extrair as métricas específicas dele
        String domainPackage = basePackage + ".user";

        StringBuilder report = new StringBuilder();
        report.append("=======================================================\n");
        report.append("MÉTRICAS ARQUITETURAIS DDD - ").append(basePackage).append("\n");
        report.append("=======================================================\n\n");

        // ---------------------------------------------------------------------
        // 3. MÉTRICAS DE COMPONENTES (Robert C. Martin - Clean Architecture)
        // ---------------------------------------------------------------------
        ComponentDependencyMetrics martinMetrics = calculateComponentDependencyMetrics(components);

        report.append("--- Component Dependency Metrics (Robert C. Martin) ---\n");
        report.append("Efferent Coupling (Ce) [Domain]                    : ").append(martinMetrics.getEfferentCoupling(domainPackage)).append("\n");
        report.append("Afferent Coupling (Ca) [Domain]                    : ").append(martinMetrics.getAfferentCoupling(domainPackage)).append("\n");
        report.append("Instability (I) [Domain]                           : ").append(martinMetrics.getInstability(domainPackage)).append("\n");
        report.append("Abstractness (A) [Domain]                          : ").append(martinMetrics.getAbstractness(domainPackage)).append("\n");
        report.append("Normalized Distance from Main Sequence (D) [Domain]: ").append(martinMetrics.getNormalizedDistanceFromMainSequence(domainPackage)).append("\n");

        // ---------------------------------------------------------------------
        // 4. MÉTRICAS DE LAKOS (John Lakos - Dependência Acumulada)
        // ---------------------------------------------------------------------
        LakosMetrics lakosMetrics = calculateLakosMetrics(components);

        report.append("\n--- Lakos Metrics (Análise de Acoplamento Global) ---\n");
        report.append("Cumulative Component Dependency (CCD)              : ").append(lakosMetrics.getCumulativeComponentDependency()).append("\n");
        report.append("Average Component Dependency (ACD)                 : ").append(lakosMetrics.getAverageComponentDependency()).append("\n");
        report.append("Relative Average Component Dependency (RACD)       : ").append(lakosMetrics.getRelativeAverageComponentDependency()).append("\n");
        report.append("Normalized Cumulative Component Dependency (NCCD)  : ").append(lakosMetrics.getNormalizedCumulativeComponentDependency()).append("\n");

        // ---------------------------------------------------------------------
        // 5. MÉTRICAS DE VISIBILIDADE (Herbert Dowalil)
        // ---------------------------------------------------------------------
        VisibilityMetrics visibilityMetrics = calculateVisibilityMetrics(components);

        report.append("\n--- Visibility Metrics (Análise de Encapsulamento) ---\n");
        report.append("Relative Visibility [Domain]                       : ").append(visibilityMetrics.getRelativeVisibility(domainPackage)).append("\n");
        report.append("Average Relative Visibility                        : ").append(visibilityMetrics.getAverageRelativeVisibility()).append("\n");
        report.append("Global Relative Visibility                         : ").append(visibilityMetrics.getGlobalRelativeVisibility()).append("\n");
        report.append("=======================================================\n");

        System.out.println(report);
        Allure.addAttachment("Relatório de métricas DDD", "text/plain", report.toString(), ".txt");
    }

    @Step("Importar classes do pacote {basePackage}")
    private JavaClasses importClasses(String basePackage) {
        return new ClassFileImporter().importPackages(basePackage);
    }

    @Step("Calcular métricas de acoplamento de componentes (Robert C. Martin)")
    private ComponentDependencyMetrics calculateComponentDependencyMetrics(MetricsComponents<JavaClass> components) {
        return ArchitectureMetrics.componentDependencyMetrics(components);
    }

    @Step("Calcular métricas de Lakos")
    private LakosMetrics calculateLakosMetrics(MetricsComponents<JavaClass> components) {
        return ArchitectureMetrics.lakosMetrics(components);
    }

    @Step("Calcular métricas de visibilidade")
    private VisibilityMetrics calculateVisibilityMetrics(MetricsComponents<JavaClass> components) {
        return ArchitectureMetrics.visibilityMetrics(components);
    }
}