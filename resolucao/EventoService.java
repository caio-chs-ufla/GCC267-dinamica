package br.ufla.desafiocache.service;

import br.ufla.desafiocache.banco.BancoSimulado;
import br.ufla.desafiocache.banco.Evento;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * BUG 2 · O organizador corrige o nome de um evento.
 *
 * A lista de eventos ativos é muito lida, então fica no cache por 10 minutos.
 */
@Service
public class EventoService {

    private static final String CHAVE_EVENTOS_ATIVOS = "eventos:ativos";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final BancoSimulado banco;
    private final Json json;

    public EventoService(StringRedisTemplate redis, BancoSimulado banco, Json json) {
        this.redis = redis;
        this.banco = banco;
        this.json = json;
    }

    public List<Evento> listarAtivos() {
        String emCache = redis.opsForValue().get(CHAVE_EVENTOS_ATIVOS);
        if (emCache != null) {
            return json.ler(emCache, new TypeReference<List<Evento>>() { });
        }

        List<Evento> eventos = banco.listarEventosAtivos();
        redis.opsForValue().set(CHAVE_EVENTOS_ATIVOS, json.escrever(eventos), TTL);
        return eventos;
    }

    public void renomear(long eventoId, String novoNome) {
        banco.atualizarNomeDoEvento(eventoId, novoNome);
        redis.delete(CHAVE_EVENTOS_ATIVOS);
    }
}
