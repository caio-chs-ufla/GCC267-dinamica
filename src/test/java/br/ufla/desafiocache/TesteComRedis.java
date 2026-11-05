package br.ufla.desafiocache;

import br.ufla.desafiocache.banco.BancoSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Base dos testes: usa o banco lógico 1 do Redis (o app usa o 0),
 * limpa tudo antes de cada teste e zera o banco simulado.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.data.redis.database=1")
public abstract class TesteComRedis {

    @Autowired
    protected StringRedisTemplate redis;

    @Autowired
    protected BancoSimulado banco;

    @BeforeEach
    void limparTudo() {
        try (RedisConnection conexao = redis.getRequiredConnectionFactory().getConnection()) {
            conexao.serverCommands().flushDb();
        }
        banco.reiniciar();
    }

    /**
     * Executa {@code total} chamadas usando {@code processos} threads,
     * todas liberadas ao mesmo tempo, e devolve os resultados.
     */
    protected <T> List<T> emParalelo(int processos, int total, Callable<T> tarefa) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(processos);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<T>> futuros = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return tarefa.call();
                }));
            }
            largada.countDown();

            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) {
                resultados.add(futuro.get());
            }
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }
}
