# Bug 1 · Cada um vê o próprio perfil

**Arquivo:** `src/main/java/br/ufla/desafiocache/service/PerfilService.java`
**Método:** `buscarPerfil`

## O problema

```java
String chave = "perfil";
```

A chave é a mesma para todos os usuários. A Ana dá miss, o perfil dela é gravado em `perfil`, e o Bruno dá hit nessa mesma chave.

## A correção

```java
String chave = "perfil:" + userId;
```

## Motivo do bug

A chave do cache não inclui o `userId`, que é justamente o que muda a resposta. Todos os usuários passam a compartilhar a mesma entrada no Redis, e o primeiro que acessa "contamina" o cache para os demais.

Regra geral: tudo o que muda a resposta (usuário, idioma, página, filtros, permissões) precisa fazer parte da chave.
