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
import jakarta.persistence.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Valida o design das entidades e a separação da lógica de domínio.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class EntityArchitectureTest {

    @ArchTest
    static final ArchRule entidades_sao_anotadas_com_entity =
        classes().that().resideInAPackage("..domain.entity..")
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(Entity.class)
            .as("Classes de entidade devem estar anotadas com @Entity");

    @ArchTest
    static final ArchRule apenas_entidades_usam_entity =
        classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..domain.entity..")
            .as("Apenas classes de entidade devem usar a anotação @Entity");

    @ArchTest
    static final ArchRule entidades_nao_dependem_de_repositorios =
        noClasses().that().resideInAPackage("..domain.entity..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .as("Entidades não devem depender de repositórios");

    @ArchTest
    static final ArchRule entidades_nao_dependem_de_servicos =
        noClasses().that().resideInAPackage("..domain.entity..")
            .should().dependOnClassesThat().resideInAPackage("..service..")
            .as("Entidades não devem depender de serviços");

    @ArchTest
    static final ArchRule entidade_user_possui_metodos_de_negocio =
        classes().that().haveSimpleName("User")
            .should(possuirMetodos("getFullName", "activate", "deactivate"))
            .as("A entidade User deve possuir métodos de negócio (getFullName, activate, deactivate)");

    private static ArchCondition<JavaClass> possuirMetodos(String... nomesEsperados) {
        List<String> esperados = Arrays.asList(nomesEsperados);
        return new ArchCondition<>("possuir os métodos " + esperados) {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                List<String> existentes = clazz.getMethods().stream()
                    .map(JavaMethod::getName)
                    .collect(Collectors.toList());
                List<String> ausentes = esperados.stream()
                    .filter(nome -> !existentes.contains(nome))
                    .collect(Collectors.toList());
                boolean satisfeito = ausentes.isEmpty();
                String mensagem = satisfeito
                    ? clazz.getName() + " possui todos os métodos de negócio esperados"
                    : clazz.getName() + " não possui os métodos: " + ausentes;
                events.add(new SimpleConditionEvent(clazz, satisfeito, mensagem));
            }
        };
    }
}
