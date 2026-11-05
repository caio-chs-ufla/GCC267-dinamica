# Desafio: conserte o cache

Dinâmica prática da apresentação **T09 · Cache e desempenho com Redis** (GCC267 Projeto Integrador · 2026/2).

O projeto é uma API Spring Boot que usa Redis como cache. Ela funciona, mas tem **4 bugs de cache**. Cada bug é um dos problemas que vimos nos slides. A sua equipe precisa encontrar e corrigir o máximo possível em **8 minutos**.

O "banco de dados" é simulado: cada consulta demora 100 ms e é contada. Assim os testes conseguem ver quantas vezes alguém foi ao banco.

## Antes da aula (faça em casa)

Você precisa de **Java 17 ou mais novo** e **Docker**.

```bash
# 1. Suba o Redis
docker compose up -d

# 2. Rode o verificador uma vez (baixa as dependências e mostra o placar)
verificar.cmd        # Windows
./verificar.sh       # Linux / macOS
```

Se aparecer `PLACAR: 0 de 4 bugs corrigidos`, está tudo pronto. É assim mesmo: os bugs ainda estão lá.

## Como jogar

1. Leia a descrição dos bugs abaixo.
2. Encontre o código com problema em `src/main/java/br/ufla/desafiocache/service/`.
3. Corrija e rode o verificador de novo.
4. Cada bug corrigido vale **1 ponto**. Vence a equipe com mais pontos.

Regras:

- **Não mexa nos testes** (`src/test`), no `BancoSimulado` nem no `Json`.
- Não vale "resolver" tirando o cache. Os testes também conferem que o cache continua funcionando.
- Um bug só conta quando **todos** os testes dele passam.

## Os bugs

### Bug 1 · Cada um vê o próprio perfil

A Ana abre o perfil dela. Um minuto depois, o Bruno abre o dele e aparece o nome e o e-mail da Ana.

- Teste: `Bug1PerfilTest`
- Endpoint para ver na mão: `GET /perfil/1` e depois `GET /perfil/2`
- Slide relacionado: *Tudo o que muda a resposta precisa estar na chave.*

### Bug 2 · O nome novo do evento aparece na lista

O organizador corrige o nome de um evento. O banco é atualizado, mas a lista de eventos continua mostrando o nome antigo por até 10 minutos.

- Teste: `Bug2EventoTest`
- Endpoint: `GET /eventos`, depois `PUT /eventos/1/nome?nome=Novo` e `GET /eventos` de novo
- Slide relacionado: *Grave no banco e apague a chave.*

### Bug 3 · Não vender mais ingressos do que existem

Na abertura de vendas, 40 processos tentam comprar 400 vezes ao mesmo tempo um evento com 100 ingressos. O sistema vende bem mais do que 100.

- Teste: `Bug3IngressoTest`
- Endpoints: `POST /ingressos/7/abrir?quantidade=100` e `POST /ingressos/7/comprar`
- Slide relacionado: *Ler e depois escrever vende mais do que existe.*

### Bug 4 (bônus) · Stampede quando o ranking expira

O ranking é caro de calcular e fica 60 segundos no cache. Quando a chave expira, chegam 200 pedidos juntos e dezenas deles vão ao banco ao mesmo tempo para recalcular a mesma coisa. Só um deveria recalcular; os outros esperam.

- Teste: `Bug4RankingTest`
- Endpoint: `GET /ranking` e `GET /banco/consultas`
- Slide relacionado: *Stampede: uma chave expira e todos vão ao banco.*

## Comandos úteis

```bash
# Rodar só um bug
./mvnw test -Dtest=Bug1PerfilTest          # Windows: mvnw.cmd test -Dtest=Bug1PerfilTest

# Subir a API em http://localhost:8080
./mvnw spring-boot:run

# Olhar o que está no Redis
docker exec -it desafio-cache-redis redis-cli
> KEYS *
> GET perfil
> TTL eventos:ativos
```

Os testes usam o banco lógico 1 do Redis (`SELECT 1` no `redis-cli`), e a API usa o 0.

## Problemas comuns

| Sintoma | Solução |
|---|---|
| `Unable to connect to Redis` ou aviso no placar | Rode `docker compose up -d` e confira com `docker ps` |
| Porta 6379 ocupada | Já existe outro Redis rodando. Pare-o ou use ele mesmo |
| `mvnw` não executa no Linux/macOS | `chmod +x mvnw verificar.sh` |
| Erro de versão do Java | Instale o JDK 17 ou mais novo e confira com `java -version` |

---

> **Para quem apresenta:** a pasta `resolucao/` tem o gabarito. Apague essa pasta antes de passar o projeto para as equipes.
