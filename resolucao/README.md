# Gabarito

> Não entregue esta pasta para as equipes.

| Bug | Arquivo a corrigir | O que muda |
|---|---|---|
| 1 · Perfil | `src/main/java/br/ufla/desafiocache/service/PerfilService.java` | Colocar o `userId` na chave |
| 2 · Evento | `src/main/java/br/ufla/desafiocache/service/EventoService.java` | Apagar a chave depois de gravar no banco |
| 3 · Ingressos | `src/main/java/br/ufla/desafiocache/service/IngressoService.java` | Trocar GET + SET por uma operação atômica |
| 4 · Ranking (bônus) | `src/main/java/br/ufla/desafiocache/service/RankingService.java` | Lock com `SET NX EX` para só um processo recalcular |

Cada arquivo `bug-N-*.md` mostra o trecho com problema, a correção e o que falar ao explicar.

Para conferir o gabarito inteiro de uma vez, copie os arquivos `.java` desta pasta para `src/main/java/br/ufla/desafiocache/service/` e rode `verificar.cmd`. O placar deve mostrar `4 de 4`.

## Sugestão de roteiro (8 min)

| Tempo | O que acontece |
|---|---|
| 0:00 | Projetar o placar com `0 de 4` e ler os bugs do README |
| 0:30 | Equipes corrigem. Quem fechar um bug levanta a mão e mostra o placar |
| 6:00 | Parar. Mostrar a correção dos bugs 1 a 3 (1 min cada, no máximo) |
| 8:00 | Placar final; o bug 4 vira gancho para o slide de stampede |
