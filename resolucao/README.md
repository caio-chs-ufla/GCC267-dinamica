# Gabarito

> Esta pasta contém as respostas. Remova-a antes de distribuir o projeto.

| Bug | Arquivo a corrigir | O que muda |
|---|---|---|
| 1 · Perfil | `src/main/java/br/ufla/desafiocache/service/PerfilService.java` | Colocar o `userId` na chave |
| 2 · Evento | `src/main/java/br/ufla/desafiocache/service/EventoService.java` | Apagar a chave depois de gravar no banco |
| 3 · Ingressos | `src/main/java/br/ufla/desafiocache/service/IngressoService.java` | Trocar GET + SET por uma operação atômica |
| 4 · Ranking (bônus) | `src/main/java/br/ufla/desafiocache/service/RankingService.java` | Lock com `SET NX EX` para só um processo recalcular |

Cada arquivo `bug-N-*.md` mostra o trecho com problema, a correção e o motivo do bug.

Para conferir o gabarito inteiro de uma vez, copie os arquivos `.java` desta pasta para `src/main/java/br/ufla/desafiocache/service/` e rode `./verificar.sh` (Windows: `verificar.cmd`). O placar deve mostrar `4 de 4`.
