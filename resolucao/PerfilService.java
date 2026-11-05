package br.ufla.desafiocache.service;

import br.ufla.desafiocache.banco.BancoSimulado;
import br.ufla.desafiocache.banco.Usuario;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * BUG 1 · Cada usuário abre o próprio perfil.
 *
 * Leitura com cache-aside: procura no Redis; se não achar, busca no banco
 * e guarda por 5 minutos.
 */
@Service
public class PerfilService {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;
    private final BancoSimulado banco;
    private final Json json;

    public PerfilService(StringRedisTemplate redis, BancoSimulado banco, Json json) {
        this.redis = redis;
        this.banco = banco;
        this.json = json;
    }

    public Usuario buscarPerfil(long userId) {
        String chave = "perfil:" + userId;

        String emCache = redis.opsForValue().get(chave);
        if (emCache != null) {
            return json.ler(emCache, Usuario.class);
        }

        Usuario usuario = banco.buscarUsuario(userId);
        redis.opsForValue().set(chave, json.escrever(usuario), TTL);
        return usuario;
    }
}
