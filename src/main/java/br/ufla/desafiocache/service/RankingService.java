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
        String emCache = redis.opsForValue().get(CHAVE_RANKING);
        if (emCache != null) {
            return json.ler(emCache, new TypeReference<List<ItemRanking>>() { });
        }

        List<ItemRanking> ranking = banco.calcularRanking();
        redis.opsForValue().set(CHAVE_RANKING, json.escrever(ranking), TTL);
        return ranking;
    }
}
