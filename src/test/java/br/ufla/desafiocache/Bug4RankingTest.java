package br.ufla.desafiocache;

import br.ufla.desafiocache.banco.ItemRanking;
import br.ufla.desafiocache.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bug 4 (bônus) · Stampede quando o ranking expira")
class Bug4RankingTest extends TesteComRedis {

    @Autowired
    RankingService ranking;

    @Test
    @DisplayName("200 pedidos chegam juntos logo depois de a chave expirar")
    void soUmProcessoRecalcula() throws Exception {
        // O cache acabou de expirar: a chave não existe (limparTudo apagou).
        List<List<ItemRanking>> respostas = emParalelo(40, 200, () -> ranking.ranking());

        int consultas = banco.totalDeConsultas();
        assertTrue(consultas <= 2,
                consultas + " processos foram ao banco ao mesmo tempo para recalcular o ranking. "
                        + "Deveria ser 1 (no máximo 2).");

        boolean todosReceberam = respostas.stream().allMatch(r -> r != null && r.size() == 3);
        assertTrue(todosReceberam, "Todos os 200 pedidos deveriam receber o ranking completo.");
    }

    @Test
    @DisplayName("Com o ranking em cache, ninguém vai ao banco")
    void rankingEmCacheNaoConsulta() {
        ranking.ranking();
        ranking.ranking();

        assertEquals(1, banco.totalDeConsultas(), "A segunda leitura deveria vir do cache.");
    }
}
