package com.example.user.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Valida o uso de cache Redis na camada de aplicação (Use Cases/Services).
 *
 * Verifica se:
 * - Services utilizam @Cacheable para operações de leitura
 * - Cache está configurado com chaves apropriadas
 * - Operações de escrita utilizam @CacheEvict para invalidar cache
 * - A configuração de cache está centralizada
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class RedisCacheArchitectureTest {

    @ArchTest
    static final ArchRule configuracao_cache_deve_estar_centralizada =
        classes().that().haveNameMatching(".*CacheConfig.*|.*CacheConfiguration.*")
            .and().haveNameNotMatching(".*Test.*")
            .should().resideInAPackage("com.example.config")
            .allowEmptyShould(true)
            .as("Configuração de Cache deve estar centralizada em um único local (com.example.config)");

    @ArchTest
    static final ArchRule cache_deve_ser_habilitado_na_config =
        classes().that().resideInAPackage("com.example.config")
            .and().haveNameMatching(".*CacheConfig.*")
            .should().beAnnotatedWith("org.springframework.cache.annotation.EnableCaching")
            .allowEmptyShould(true)
            .as("Configuração de cache deve anotar com @EnableCaching");

    @ArchTest
    static final ArchRule get_use_cases_devem_utilizar_cacheable =
        classes().that().haveNameMatching("Get.*UseCase|List.*UseCase")
            .and().resideInAPackage("..usecase..")
            .and().haveNameNotMatching(".*Test.*")
            .should(utilizarCacheableOuCachePut())
            .allowEmptyShould(true)
            .as("Use cases de leitura (Get/List) devem utilizar @Cacheable ou @CachePut");

    @ArchTest
    static final ArchRule create_update_delete_use_cases_devem_invalidar_cache =
        classes().that().haveNameMatching("Create.*UseCase|Update.*UseCase|Delete.*UseCase")
            .and().resideInAPackage("..usecase..")
            .and().haveNameNotMatching(".*Test.*")
            .should(utilizarCacheEvict())
            .allowEmptyShould(true)
            .as("Use cases de escrita (Create/Update/Delete) devem utilizar @CacheEvict para invalidar cache");

    @ArchTest
    static final ArchRule methods_com_cacheable_devem_ter_chave_explicitamente_definida =
        methods().that().areAnnotatedWith("org.springframework.cache.annotation.Cacheable")
            .and().areDeclaredInClassesThat().resideInAPackage("..usecase..")
            .should(possuirCacheKeyDefinida())
            .allowEmptyShould(true)
            .as("Métodos com @Cacheable devem definir a chave de cache explicitamente (key=\"#...\")");

    @ArchTest
    static final ArchRule cache_config_deve_ser_anotada_como_configuration =
        classes().that().haveNameMatching(".*CacheConfig.*")
            .should().beAnnotatedWith("org.springframework.context.annotation.Configuration")
            .allowEmptyShould(true)
            .as("CacheConfig deve ser anotada com @Configuration");

    private static ArchCondition<JavaClass> utilizarCacheableOuCachePut() {
        return new ArchCondition<JavaClass>("utilizar @Cacheable ou @CachePut") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean temCacheAnnotation = javaClass.getMethods().stream()
                    .anyMatch(method -> method.getAnnotations().stream()
                        .anyMatch(ann -> ann.getRawType().getName()
                            .matches("org\\.springframework\\.cache\\.annotation\\.(Cacheable|CachePut)")));

                if (!temCacheAnnotation) {
                    String message = "não possui métodos anotados com @Cacheable ou @CachePut";
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> utilizarCacheEvict() {
        return new ArchCondition<JavaClass>("utilizar @CacheEvict") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean temCacheEvict = javaClass.getMethods().stream()
                    .anyMatch(method -> method.getAnnotations().stream()
                        .anyMatch(ann -> ann.getRawType().getName()
                            .equals("org.springframework.cache.annotation.CacheEvict")));

                if (!temCacheEvict) {
                    String message = "não possui métodos anotados com @CacheEvict";
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaMethod> possuirCacheKeyDefinida() {
        return new ArchCondition<JavaMethod>("possuir chave de cache definida") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getAnnotations().stream()
                    .filter(ann -> ann.getRawType().getName()
                        .equals("org.springframework.cache.annotation.Cacheable"))
                    .forEach(cacheableAnnotation -> {
                        Object keyValue = cacheableAnnotation.get("key");
                        if (keyValue == null || (keyValue instanceof String && ((String) keyValue).isEmpty())) {
                            String message = "não define a chave de cache (key=\"#...\")";
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                    });
            }
        };
    }
}
