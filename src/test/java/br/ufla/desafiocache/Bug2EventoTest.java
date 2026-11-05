package br.ufla.desafiocache;

import br.ufla.desafiocache.banco.Evento;
import br.ufla.desafiocache.service.EventoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Bug 2 · O nome novo do evento aparece na lista")
class Bug2EventoTest extends TesteComRedis {

    @Autowired
    EventoService eventos;

    @Test
    @DisplayName("Depois de renomear, a lista mostra o nome novo")
    void listaMostraONomeNovo() {
        eventos.listarAtivos(); // alguém abre a lista e ela vai para o cache

        eventos.renomear(1, "Show de Rock - Data Extra");

        List<Evento> lista = eventos.listarAtivos();
        assertEquals("Show de Rock - Data Extra", lista.get(0).nome(),
                "O evento foi renomeado no banco, mas a lista ainda mostra \"" + lista.get(0).nome()
                        + "\". Quem lê o cache vai ver o nome velho por até 10 minutos.");
    }

    @Test
    @DisplayName("Sem alteração, a lista continua vindo do cache")
    void listaContinuaEmCache() {
        eventos.listarAtivos();
        eventos.listarAtivos();
        eventos.listarAtivos();

        assertEquals(1, banco.totalDeConsultas(),
                "Três leituras seguidas da lista deveriam ir ao banco só uma vez.");
    }
}
