package br.ufla.desafiocache.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    /** Tenta comprar 1 ingresso. Devolve true se a compra deu certo. */
    public boolean comprar(long eventoId) {
        String chave = chave(eventoId);

        int restantes = restantes(eventoId);
        if (restantes <= 0) {
            return false;
        }

        processarPedido();
        redis.opsForValue().set(chave, String.valueOf(restantes - 1));
        return true;
    }

    private String chave(long eventoId) {
        return "ingressos:" + eventoId;
    }

    /** Simula validações do pedido entre a leitura e a escrita (1 ms). */
    private void processarPedido() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
