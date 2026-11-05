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

## Para explicar

Tudo o que muda a resposta precisa estar na chave: usuário, idioma, página, filtros, permissões. Uma forma de achar esse bug é olhar os parâmetros do método e conferir se cada um aparece na chave.
