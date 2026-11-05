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

## Motivo do bug

A escrita atualiza o banco, mas não invalida o cache. A cópia guardada em `eventos:ativos` fica desatualizada até o TTL expirar.

Ao gravar no banco e em seguida apagar a chave, a próxima leitura dá miss e já traz o valor novo. Diminuir o TTL não resolve: só reduz a janela em que o dado errado aparece e faz o cache perder o sentido.
