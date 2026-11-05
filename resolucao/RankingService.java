package br.ufla.desafiocache.service;

import br.ufla.desafiocache.banco.BancoSimulado;
import br.ufla.desafiocache.banco.ItemRanking;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * BUG 4 (bônus) · O ranking é caro de calcular e todo mundo olha para ele.
 *
 * Fica no cache por 60 segundos. O problema aparece no instante em que
 * a chave expira e chegam muitos pedidos juntos.
 */
@Service
public class RankingService {

    private static final String CHAVE_RANKING = "ranking:geral";
    private static final String CHAVE_LOCK = "lock:ranking:geral";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final BancoSimulado banco;
    private final Json json;

    public RankingService(StringRedisTemplate redis, BancoSimulado banco, Json json) {
        this.redis = redis;
        this.banco = banco;
        this.json = json;
    }

    public List<ItemRanking> ranking() {
        List<ItemRanking> emCache = lerDoCache();
        if (emCache != null) {
            return emCache;
        }

        // SET lock:ranking:geral 1 NX EX 5
        Boolean peguei = redis.opsForValue().setIfAbsent(CHAVE_LOCK, "1", Duration.ofSeconds(5));
        if (Boolean.TRUE.equals(peguei)) {
            try {
                // Confere de novo: outro processo pode ter acabado de preencher o cache
                emCache = lerDoCache();
                if (emCache != null) {
                    return emCache;
                }
                List<ItemRanking> ranking = banco.calcularRanking();
                redis.opsForValue().set(CHAVE_RANKING, json.escrever(ranking), TTL);
                return ranking;
            } finally {
                redis.delete(CHAVE_LOCK);
            }
        }

        // Não peguei o lock: espero quem pegou terminar (até ~2 s)
        for (int tentativa = 0; tentativa < 100; tentativa++) {
            dormir(20);
            emCache = lerDoCache();
            if (emCache != null) {
                return emCache;
            }
        }
        return banco.calcularRanking(); // último recurso, se quem tinha o lock travou
    }

    private List<ItemRanking> lerDoCache() {
        String texto = redis.opsForValue().get(CHAVE_RANKING);
        return texto == null ? null : json.ler(texto, new TypeReference<List<ItemRanking>>() { });
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
