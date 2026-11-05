# Bug 4 (bônus) · Stampede quando o ranking expira

**Arquivo:** `src/main/java/br/ufla/desafiocache/service/RankingService.java`
**Método:** `ranking`

## O problema

```java
List<ItemRanking> ranking = banco.calcularRanking();
```

Quando a chave expira, todos os pedidos que chegam naquele instante dão miss e vão ao banco ao mesmo tempo para calcular a mesma coisa.

## A correção

Só quem conseguir o lock recalcula. Os outros esperam o valor aparecer no cache.

```java
private static final String CHAVE_LOCK = "lock:ranking:geral";

public List<ItemRanking> ranking() {
    List<ItemRanking> emCache = lerDoCache();
    if (emCache != null) {
        return emCache;
    }

    // SET lock:ranking:geral 1 NX EX 5
    Boolean peguei = redis.opsForValue().setIfAbsent(CHAVE_LOCK, "1", Duration.ofSeconds(5));
    if (Boolean.TRUE.equals(peguei)) {
        try {
            // Confere de novo: outro processo pode ter acabado de preencher o cache
            emCache = lerDoCache();
            if (emCache != null) {
                return emCache;
            }
            List<ItemRanking> ranking = banco.calcularRanking();
            redis.opsForValue().set(CHAVE_RANKING, json.escrever(ranking), TTL);
            return ranking;
        } finally {
            redis.delete(CHAVE_LOCK);
        }
    }

    // Não peguei o lock: espero quem pegou terminar (até ~2 s)
    for (int tentativa = 0; tentativa < 100; tentativa++) {
        dormir(20);
        emCache = lerDoCache();
        if (emCache != null) {
            return emCache;
        }
    }
    return banco.calcularRanking(); // último recurso, se quem tinha o lock travou
}

private List<ItemRanking> lerDoCache() {
    String texto = redis.opsForValue().get(CHAVE_RANKING);
    return texto == null ? null : json.ler(texto, new TypeReference<List<ItemRanking>>() { });
}

private void dormir(long ms) {
    try {
        Thread.sleep(ms);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

## Motivo do bug

Quando a chave expira, nada coordena quem vai recalcular: todos os pedidos que chegam naquele instante dão miss e vão ao banco calcular a mesma coisa (cache stampede).

Com um lock (`SET ... NX EX 5`), só um processo recalcula e os outros esperam o valor aparecer no cache. O `NX` faz o `SET` só funcionar se a chave ainda não existir, e o `EX` faz o lock expirar sozinho caso o processo morra no meio.
