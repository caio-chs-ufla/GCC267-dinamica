# Bug 3 · Não vender mais ingressos do que existem

**Arquivo:** `src/main/java/br/ufla/desafiocache/service/IngressoService.java`
**Método:** `comprar`

## O problema

```java
int restantes = restantes(eventoId);          // GET
if (restantes <= 0) {
    return false;
}
processarPedido();
redis.opsForValue().set(chave, String.valueOf(restantes - 1));   // SET
```

Vários processos leem o mesmo valor (por exemplo, 57), todos acham que há ingresso e todos gravam 56. Foram vendidos vários ingressos, mas o estoque só caiu um.

## Correção A: script Lua (a do slide)

O Redis executa o script inteiro de uma vez, sem nenhum outro comando no meio.

```java
private static final DefaultRedisScript<Long> COMPRAR = new DefaultRedisScript<>("""
        local restantes = tonumber(redis.call('GET', KEYS[1]) or '0')
        if restantes <= 0 then
            return 0
        end
        redis.call('DECR', KEYS[1])
        return 1
        """, Long.class);

public boolean comprar(long eventoId) {
    Long comprou = redis.execute(COMPRAR, List.of(chave(eventoId)));
    return comprou != null && comprou == 1L;
}
```

Imports necessários:

```java
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
```

## Correção B: DECR e desfazer se passou do zero

```java
public boolean comprar(long eventoId) {
    String chave = chave(eventoId);
    Long restantes = redis.opsForValue().decrement(chave);
    if (restantes == null || restantes < 0) {
        redis.opsForValue().increment(chave);   // devolve o que tirou
        return false;
    }
    return true;
}
```

Também passa nos testes. Vale comentar que, por um instante, o contador pode ficar negativo; com Lua isso nunca acontece.

## Para explicar

Ler, decidir e escrever em comandos separados não é atômico. Para um passo só, use `INCR`, `DECR` ou `SET NX`. Para mais de um passo, use Lua.
