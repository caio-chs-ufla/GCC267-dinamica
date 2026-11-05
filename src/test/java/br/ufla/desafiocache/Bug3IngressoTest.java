package br.ufla.desafiocache;

import br.ufla.desafiocache.service.IngressoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Bug 3 · Não vender mais ingressos do que existem")
class Bug3IngressoTest extends TesteComRedis {

    private static final long EVENTO = 7;

    @Autowired
    IngressoService ingressos;

    @Test
    @DisplayName("400 tentativas, 40 processos, 100 ingressos")
    void vendeExatamenteOEstoque() throws Exception {
        ingressos.abrirVendas(EVENTO, 100);

        List<Boolean> compras = emParalelo(40, 400, () -> ingressos.comprar(EVENTO));
        long vendidos = compras.stream().filter(Boolean::booleanValue).count();

        assertEquals(100, vendidos,
                "Foram vendidos " + vendidos + " ingressos, mas só existiam 100.");
        assertEquals(0, ingressos.restantes(EVENTO),
                "Depois de esgotar, o estoque deveria ser 0.");
    }

    @Test
    @DisplayName("Com o estoque zerado, ninguém compra")
    void estoqueZeradoNaoVende() {
        ingressos.abrirVendas(EVENTO, 0);

        assertEquals(false, ingressos.comprar(EVENTO), "Não deveria vender com estoque 0.");
        assertEquals(0, ingressos.restantes(EVENTO), "O estoque não pode ficar negativo.");
    }
}
