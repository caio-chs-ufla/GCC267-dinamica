package br.ufla.desafiocache.placar;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Map;
import java.util.TreeMap;

/**
 * Imprime o placar no fim do "mvnw test": um bug só conta como corrigido
 * quando todos os testes daquela classe passam.
 *
 * NÃO PRECISA MEXER NESTE ARQUIVO.
 */
public class Placar implements TestExecutionListener {

    private TestPlan plano;
    /** nome da classe -> todos os testes passaram? */
    private final Map<String, Boolean> resultado = new TreeMap<>();
    private final Map<String, String> titulos = new TreeMap<>();
    private boolean falhaDeConexao;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.plano = testPlan;
    }

    @Override
    public void executionFinished(TestIdentifier id, TestExecutionResult res) {
        if (plano == null) {
            return;
        }
        if (res.getThrowable().map(Placar::ehErroDeConexao).orElse(false)) {
            falhaDeConexao = true;
        }
        if (!id.isTest()) {
            // Se a classe inteira falhar (ex.: Redis fora do ar), conta como não corrigido
            id.getSource()
                    .filter(ClassSource.class::isInstance)
                    .map(ClassSource.class::cast)
                    .filter(fonte -> fonte.getJavaClass().getSimpleName().startsWith("Bug"))
                    .ifPresent(fonte -> {
                        String classe = fonte.getJavaClass().getSimpleName();
                        titulos.put(classe, id.getDisplayName());
                        resultado.putIfAbsent(classe, true);
                        if (res.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                            resultado.put(classe, false);
                        }
                    });
            return;
        }
        plano.getParent(id).ifPresent(pai -> pai.getSource()
                .filter(ClassSource.class::isInstance)
                .map(ClassSource.class::cast)
                .filter(fonte -> fonte.getJavaClass().getSimpleName().startsWith("Bug"))
                .ifPresent(fonte -> {
                    String classe = fonte.getJavaClass().getSimpleName();
                    titulos.put(classe, pai.getDisplayName());
                    boolean passou = res.getStatus() == TestExecutionResult.Status.SUCCESSFUL;
                    resultado.merge(classe, passou, Boolean::logicalAnd);
                }));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (resultado.isEmpty()) {
            return;
        }
        long corrigidos = resultado.values().stream().filter(Boolean::booleanValue).count();

        StringBuilder sb = new StringBuilder();
        String linha = "=".repeat(60);
        sb.append('\n').append(linha).append('\n');
        sb.append(String.format("  PLACAR: %d de %d bugs corrigidos%n", corrigidos, resultado.size()));
        sb.append(linha).append('\n');
        resultado.forEach((classe, ok) ->
                sb.append(String.format("  %s  %s%n", ok ? "[OK]" : "[  ]", titulos.getOrDefault(classe, classe))));
        sb.append(linha).append('\n');
        if (falhaDeConexao) {
            sb.append("  ATENCAO: nao consegui falar com o Redis em localhost:6379.\n");
            sb.append("  Rode \"docker compose up -d\" na pasta do projeto e tente de novo.\n");
            sb.append(linha).append('\n');
        }
        System.out.println(sb);
    }

    private static boolean ehErroDeConexao(Throwable erro) {
        for (Throwable t = erro; t != null; t = t.getCause()) {
            String nome = t.getClass().getName();
            if (nome.contains("RedisConnectionFailureException") || nome.contains("RedisConnectionException")) {
                return true;
            }
        }
        return false;
    }
}
