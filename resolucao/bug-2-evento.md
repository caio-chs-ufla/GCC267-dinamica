# Bug 2 · O nome novo do evento aparece na lista

**Arquivo:** `src/main/java/br/ufla/desafiocache/service/EventoService.java`
**Método:** `renomear`

## O problema

```java
public void renomear(long eventoId, String novoNome) {
    banco.atualizarNomeDoEvento(eventoId, novoNome);
}
```

O banco muda, mas a chave `eventos:ativos` continua no Redis com a lista antiga até o TTL de 10 minutos acabar.

## A correção

```java
public void renomear(long eventoId, String novoNome) {
    banco.atualizarNomeDoEvento(eventoId, novoNome);
    redis.delete(CHAVE_EVENTOS_ATIVOS);
}
```

A ordem importa: primeiro grava no banco, depois apaga a chave. Se apagar antes, outro leitor pode buscar o valor velho no banco e gravar de novo no cache antes da atualização.

## Para explicar

É a estratégia de invalidação do slide de escrita. O custo é que a próxima leitura será um miss, mas ela já traz o valor novo. Não vale trocar o TTL para 1 segundo: o teste de cache continua exigindo que leituras repetidas não vão ao banco.
