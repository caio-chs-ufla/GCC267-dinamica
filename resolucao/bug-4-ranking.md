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

## Para explicar

- `NX` faz o `SET` só funcionar se a chave não existir: é o lock.
- `EX 5` garante que o lock some sozinho se o processo morrer no meio.
- A segunda leitura do cache, depois de pegar o lock, evita recalcular quando outro processo acabou de terminar. Sem ela, o teste às vezes mostra 2 consultas em vez de 1.
- Outra opção é o TTL com jitter, que espalha as expirações de chaves diferentes. Ele não resolve o caso de uma única chave muito acessada, que é este.
