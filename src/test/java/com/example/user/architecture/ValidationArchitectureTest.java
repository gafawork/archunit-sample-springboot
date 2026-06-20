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
import jakarta.validation.constraints.NotBlank;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Garante que a validação de entrada esteja corretamente implementada nos DTOs.
 */
@AnalyzeClasses(packages = "com.example.user", importOptions = ImportOption.DoNotIncludeTests.class)
class ValidationArchitectureTest {

    @ArchTest
    static final ArchRule campos_de_request_dtos_tem_validacao =
            fields().that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Request")
                    .and().areNotStatic()
                    .should().beAnnotatedWith(NotBlank.class)
                    .as("Campos de DTOs de Request devem ter validação @NotBlank");

    @ArchTest
    static final ArchRule response_dtos_nao_expoem_senha =
            noFields().that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Response")
                    .should().haveName("password")
                    .as("DTOs de Response não devem expor o campo 'password'");



    ///////////////////////////////

    // 1. Criamos a condição customizada para ler o atributo da anotação
    private static final ArchCondition<JavaMethod> DEFINIR_EXECUTOR_CUSTOMIZADO =
            new ArchCondition<JavaMethod>("definir explicitamente um executor na anotação @Async") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    Async asyncAnnotation = method.getAnnotationOfType(Async.class);

                    // Se o atributo "value" for vazio, o desenvolvedor usou apenas @Async()
                    if (asyncAnnotation.value().trim().isEmpty()) {
                        String mensagemDeErro = String.format(
                                "O método '%s.%s' usa @Async sem especificar um executor.",
                                method.getOwner().getSimpleName(),
                                method.getName()
                        );
                        events.add(SimpleConditionEvent.violated(method, mensagemDeErro));
                    }
                }
            };

    // 2. Aplicamos a regra aos métodos da arquitetura
    @ArchTest
    static final ArchRule metodos_async_devem_especificar_executor = methods()
            .that().areAnnotatedWith(Async.class)
            .should(DEFINIR_EXECUTOR_CUSTOMIZADO)
            .allowEmptyShould(true)
            .because("O @Async sem parâmetro utiliza o 'SimpleAsyncTaskExecutor' por padrão, que não reaproveita threads e cria uma nova para cada chamada. Isso causa risco de OutOfMemory (OOM). Configure um ThreadPoolTaskExecutor e o referencie pelo nome, ex: @Async(\"meuExecutorCustomizado\").");


    private static final ArchCondition<JavaClass> VERIFICAR_RELACOES_DE_AGENDAMENTO =
            new ArchCondition<JavaClass>("garantir ThreadPoolTaskScheduler caso exista uso de @Scheduled") {

                private boolean aplicacaoUsaScheduled = false;
                private boolean possuiBeanTaskScheduler = false;

                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    // 1. Verifica se a classe atual possui algum método anotado com @Scheduled
                    for (JavaMethod method : javaClass.getMethods()) {
                        if (method.isAnnotatedWith(Scheduled.class)) {
                            aplicacaoUsaScheduled = true;
                            break; // Já sabemos que a aplicação usa, não precisa checar o resto desta classe
                        }
                    }

                    // 2. Verifica se a classe atual é uma @Configuration e expõe o Bean correto
                    if (javaClass.isAnnotatedWith(Configuration.class)) {
                        for (JavaMethod method : javaClass.getMethods()) {
                            if (method.isAnnotatedWith(Bean.class) &&
                                    method.getRawReturnType().isAssignableTo(ThreadPoolTaskScheduler.class)) {
                                possuiBeanTaskScheduler = true;
                                break;
                            }
                        }
                    }
                }

                // Este método é invocado AUTOMATICAMENTE pelo ArchUnit após analisar TODAS as classes
                @Override
                public void finish(ConditionEvents events) {
                    // Aplica a regra condicional que você sugeriu
                    if (aplicacaoUsaScheduled && !possuiBeanTaskScheduler) {
                        events.add(SimpleConditionEvent.violated(
                                "Uso de @Scheduled detectado",
                                "A aplicação utiliza agendamentos (@Scheduled), mas nenhum @Bean do tipo " +
                                        "ThreadPoolTaskScheduler foi configurado. O Spring usará o pool padrão de 1 única thread!"
                        ));
                    }
                }
            };

    @ArchTest
    static final ArchRule se_usar_scheduled_deve_configurar_scheduler_customizado = classes()
            .should(VERIFICAR_RELACOES_DE_AGENDAMENTO)
            .because("Evita que múltiplas tarefas agendadas (@Scheduled) entrem em colisão ou fiquem bloqueadas mutuamente pela thread única padrão do Spring.");

    @ArchTest
    static final ArchRule controllers_nao_devem_conhecer_entidades = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("Controladores não devem trafegar entidades do banco de dados diretamente. Utilize DTOs (Data Transfer Objects) para desacoplar o contrato da API do modelo de persistência.");

    @ArchTest
    static final ArchRule transacoes_nao_devem_ficar_no_controller = noMethods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(Transactional.class)
            .because("Abrir transações no Controller prende a conexão com o banco de dados durante a leitura da requisição e escrita do JSON de resposta. O @Transactional deve ser restrito à camada de @Service.");


    @ArchTest
    static final ArchRule service_should_not_access_entity_manager =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should()
                    .accessClassesThat()
                    .haveSimpleName("EntityManager");

    @ArchTest
    static final ArchRule controller_should_not_access_repository =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule repository_only_inside_persistence =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .resideInAPackage("..repository..");






}