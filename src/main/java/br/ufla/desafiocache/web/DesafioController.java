package br.ufla.desafiocache.web;

import br.ufla.desafiocache.banco.BancoSimulado;
import br.ufla.desafiocache.banco.Evento;
import br.ufla.desafiocache.banco.ItemRanking;
import br.ufla.desafiocache.banco.Usuario;
import br.ufla.desafiocache.service.EventoService;
import br.ufla.desafiocache.service.IngressoService;
import br.ufla.desafiocache.service.PerfilService;
import br.ufla.desafiocache.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints para testar na mão (navegador, curl ou Postman).
 * NÃO PRECISA MEXER NESTE ARQUIVO.
 */
@RestController
public class DesafioController {

    private final PerfilService perfis;
    private final EventoService eventos;
    private final IngressoService ingressos;
    private final RankingService ranking;
    private final BancoSimulado banco;

    public DesafioController(PerfilService perfis, EventoService eventos, IngressoService ingressos,
                             RankingService ranking, BancoSimulado banco) {
        this.perfis = perfis;
        this.eventos = eventos;
        this.ingressos = ingressos;
        this.ranking = ranking;
        this.banco = banco;
    }

    @GetMapping("/perfil/{userId}")
    public Usuario perfil(@PathVariable long userId) {
        return perfis.buscarPerfil(userId);
    }

    @GetMapping("/eventos")
    public List<Evento> eventos() {
        return eventos.listarAtivos();
    }

    @PutMapping("/eventos/{id}/nome")
    public List<Evento> renomear(@PathVariable long id, @RequestParam String nome) {
        eventos.renomear(id, nome);
        return eventos.listarAtivos();
    }

    @PostMapping("/ingressos/{eventoId}/abrir")
    public Map<String, Integer> abrir(@PathVariable long eventoId, @RequestParam(defaultValue = "100") int quantidade) {
        ingressos.abrirVendas(eventoId, quantidade);
        return Map.of("restantes", ingressos.restantes(eventoId));
    }

    @PostMapping("/ingressos/{eventoId}/comprar")
    public Map<String, Object> comprar(@PathVariable long eventoId) {
        boolean comprou = ingressos.comprar(eventoId);
        return Map.of("comprou", comprou, "restantes", ingressos.restantes(eventoId));
    }

    @GetMapping("/ranking")
    public List<ItemRanking> ranking() {
        return ranking.ranking();
    }

    @GetMapping("/banco/consultas")
    public Map<String, Integer> consultasAoBanco() {
        return Map.of("consultas", banco.totalDeConsultas());
    }
}
