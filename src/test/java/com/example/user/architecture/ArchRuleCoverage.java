package com.example.user.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Check customizado para regras de arquitetura.
 *
 * <p>Em vez de delegar diretamente para {@link ArchRule#check(JavaClasses)}, este utilitário
 * intercepta a avaliação para contabilizar, por regra:
 * <ul>
 *     <li>quantos arquivos (classes) foram <b>filtrados</b> pela cláusula {@code .that(...)} —
 *         ou seja, o universo ao qual a regra realmente se aplica;</li>
 *     <li>quais (e quantos) desses arquivos <b>violaram</b> a regra;</li>
 *     <li>o <b>percentual de falhas</b> da regra (arquivos com falha / arquivos filtrados).</li>
 * </ul>
 *
 * <p>Após registrar as métricas e listar os arquivos infratores, mantém a semântica original do
 * {@code @ArchTest}: o teste falha caso exista qualquer violação.
 */
final class ArchRuleCoverage {

    private static final Logger log = LoggerFactory.getLogger(ArchRuleCoverage.class);

    private ArchRuleCoverage() {
    }

    /**
     * @param rule    a regra de arquitetura a ser verificada
     * @param subject o mesmo predicado usado no {@code .that(...)} da regra, reutilizado aqui para
     *                contar — sem divergência — o conjunto de classes filtradas
     * @param classes as classes importadas pelo {@code @AnalyzeClasses}
     * @return as métricas coletadas (útil para asserções adicionais ou relatórios)
     */
    static Metrics check(ArchRule rule, DescribedPredicate<? super JavaClass> subject, JavaClasses classes) {
        List<JavaClass> filtradas = new ArrayList<>();
        for (JavaClass candidate : classes) {
            if (subject.test(candidate)) {
                filtradas.add(candidate);
            }
        }

        EvaluationResult result = rule.evaluate(classes);
        List<String> violacoes = result.getFailureReport().getDetails();

        // Um arquivo filtrado é considerado infrator quando seu nome completo aparece em alguma
        // mensagem de violação. Funciona tanto para regras de classe quanto de dependência, pois a
        // mensagem do ArchUnit sempre cita o nome qualificado da classe-sujeito.
        List<JavaClass> arquivosComFalha = filtradas.stream()
            .filter(c -> violacoes.stream().anyMatch(v -> v.contains(c.getFullName())))
            .toList();

        Metrics metrics = new Metrics(
            rule.getDescription(), filtradas.size(), arquivosComFalha, violacoes.size());

        log.info("[ArchUnit] regra=\"{}\" | filtrados={} | comFalha={} | violacoes={} | falha={}%",
            metrics.descricao(), metrics.filtradas(), metrics.arquivosComFalha().size(), metrics.violacoes(),
            String.format(Locale.ROOT, "%.1f", metrics.percentualFalha()));

        if (!arquivosComFalha.isEmpty()) {
            log.warn("[ArchUnit] arquivos que violaram a regra \"{}\":", metrics.descricao());
            for (JavaClass infrator : arquivosComFalha) {
                log.warn("    - {} ({})", infrator.getName(), nomeDoArquivo(infrator));
            }
        }

        if (result.hasViolation()) {
            throw new AssertionError(result.getFailureReport().toString());
        }
        return metrics;
    }

    /** Nome do arquivo-fonte (.java) da classe, quando disponível na importação. */
    private static String nomeDoArquivo(JavaClass javaClass) {
        return javaClass.getSource()
            .flatMap(source -> source.getFileName())
            .orElseGet(() -> javaClass.getSimpleName() + ".java");
    }

    /**
     * Métricas de cobertura de uma regra de arquitetura.
     *
     * @param descricao        descrição da regra
     * @param filtradas        número de arquivos selecionados pelo {@code .that(...)}
     * @param arquivosComFalha classes que violaram a regra
     * @param violacoes        número total de mensagens de violação reportadas
     */
    record Metrics(String descricao, int filtradas, List<JavaClass> arquivosComFalha, int violacoes) {

        /** Percentual de arquivos filtrados que violaram a regra (0 quando nada foi filtrado). */
        double percentualFalha() {
            return filtradas == 0 ? 0.0 : (arquivosComFalha.size() * 100.0) / filtradas;
        }
    }
}
