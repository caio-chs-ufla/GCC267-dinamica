package br.ufla.desafiocache.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BUG 3 · Abertura de vendas: muita gente compra ao mesmo tempo.
 *
 * O estoque de cada evento é um número guardado no Redis,
 * na chave "ingressos:{eventoId}".
 */
@Service
public class IngressoService {

    private final StringRedisTemplate redis;

    public IngressoService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void abrirVendas(long eventoId, int quantidade) {
        redis.opsForValue().set(chave(eventoId), String.valueOf(quantidade));
    }

    public int restantes(long eventoId) {
        String valor = redis.opsForValue().get(chave(eventoId));
        return valor == null ? 0 : Integer.parseInt(valor);
    }

    private static final DefaultRedisScript<Long> COMPRAR = new DefaultRedisScript<>("""
            local restantes = tonumber(redis.call('GET', KEYS[1]) or '0')
            if restantes <= 0 then
                return 0
            end
            redis.call('DECR', KEYS[1])
            return 1
            """, Long.class);

    /** Tenta comprar 1 ingresso. Devolve true se a compra deu certo. */
    public boolean comprar(long eventoId) {
        Long comprou = redis.execute(COMPRAR, List.of(chave(eventoId)));
        return comprou != null && comprou == 1L;
    }

    private String chave(long eventoId) {
        return "ingressos:" + eventoId;
    }

}
