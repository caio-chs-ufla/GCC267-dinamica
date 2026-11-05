package br.ufla.desafiocache.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Faz o papel do MySQL na dinâmica.
 *
 * Toda consulta demora {@code banco.latencia-ms} (100 ms por padrão) e é contada,
 * para os testes saberem quantas vezes alguém foi ao banco.
 *
 * NÃO PRECISA MEXER NESTE ARQUIVO.
 */
@Component
public class BancoSimulado {

    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<Long, Evento> eventos = new ConcurrentHashMap<>();
    private final AtomicInteger consultas = new AtomicInteger();

    @Value("${banco.latencia-ms:100}")
    private long latenciaMs;

    public BancoSimulado() {
        reiniciar();
    }

    /** Volta os dados ao estado inicial e zera o contador de consultas. */
    public void reiniciar() {
        usuarios.clear();
        usuarios.put(1L, new Usuario(1, "Ana", "ana@arena.com"));
        usuarios.put(2L, new Usuario(2, "Bruno", "bruno@arena.com"));
        usuarios.put(3L, new Usuario(3, "Carla", "carla@arena.com"));

        eventos.clear();
        eventos.put(1L, new Evento(1, "Show de Rock", "Arena Stark"));
        eventos.put(2L, new Evento(2, "Final do Campeonato", "Estádio Central"));
        eventos.put(3L, new Evento(3, "Festival de Jazz", "Teatro Municipal"));

        consultas.set(0);
    }

    public Usuario buscarUsuario(long id) {
        simularConsulta();
        return usuarios.get(id);
    }

    public List<Evento> listarEventosAtivos() {
        simularConsulta();
        return eventos.values().stream()
                .sorted(Comparator.comparingLong(Evento::id))
                .toList();
    }

    public void atualizarNomeDoEvento(long id, String novoNome) {
        simularConsulta();
        eventos.computeIfPresent(id, (chave, evento) -> evento.comNome(novoNome));
    }

    /** Consulta pesada: agrega as vendas de todos os eventos. */
    public List<ItemRanking> calcularRanking() {
        simularConsulta();
        return List.of(
                new ItemRanking(1, "Final do Campeonato", 1840),
                new ItemRanking(2, "Show de Rock", 1210),
                new ItemRanking(3, "Festival de Jazz", 430));
    }

    /** Quantas consultas chegaram ao banco desde o último {@link #reiniciar()}. */
    public int totalDeConsultas() {
        return consultas.get();
    }

    private void simularConsulta() {
        consultas.incrementAndGet();
        try {
            Thread.sleep(latenciaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
