package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class DependencyMetricsTest {

    private Set<JavaPackage> getSubpackages() {
        return new ClassFileImporter()
                .importPackages("com.example")
                .getPackage("com.example")
                .getSubpackages();
    }

    @Test
    void cumulativeDependencyMetrics() {
        final var packages = getSubpackages();
        final var components = MetricsComponents.fromPackages(packages);
        final var metrics = ArchitectureMetrics.lakosMetrics(components);

        System.out.println("Cumulative Component Dependency: " + metrics.getCumulativeComponentDependency());
        System.out.println("Average Component Dependency: " + metrics.getAverageComponentDependency());
        System.out.println("Relative Average Component Dependency: " + metrics.getRelativeAverageComponentDependency());
        System.out.println("Normalized Cumulative Component Dependency: " + metrics.getNormalizedCumulativeComponentDependency());
    }

    @Test
    void componentDependencyMetrics() {
        final var packages = getSubpackages();
        final var components = MetricsComponents.fromPackages(packages);
        final var metrics = ArchitectureMetrics.componentDependencyMetrics(components);

        packages.stream().findFirst().ifPresent(pkg -> {
            System.out.println("Efferent Coupling: " + metrics.getEfferentCoupling(pkg.getName()));
            System.out.println("Afferent coupling: " + metrics.getAfferentCoupling(pkg.getName()));
            System.out.println("Instability: " + metrics.getInstability(pkg.getName()));
            System.out.println("Abstractness: " + metrics.getAbstractness(pkg.getName()));
            System.out.println("Normalized distance from main sequence: " + metrics.getNormalizedDistanceFromMainSequence(pkg.getName()));
        });
    }

    @Test
    void visibilityMetrics() {
        final var packages = getSubpackages();
        final var components = MetricsComponents.fromPackages(packages);
        final var metrics = ArchitectureMetrics.visibilityMetrics(components);

        packages.stream().findFirst().ifPresent(pkg -> {
            System.out.println("Relative Visibility : " + metrics.getRelativeVisibility(pkg.getName()));
            System.out.println("Average Relative Visibility: " + metrics.getAverageRelativeVisibility());
            System.out.println("Global Relative Visibility: " + metrics.getGlobalRelativeVisibility());
        });
    }
}
