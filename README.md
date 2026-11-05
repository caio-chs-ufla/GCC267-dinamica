# Desafio: conserte o cache

Exercício prático de **cache e desempenho com Redis** (GCC267 Projeto Integrador · 2026/2).

O projeto é uma API Spring Boot que usa Redis como cache. Ela funciona, mas tem **4 bugs de cache**. O objetivo é encontrar e corrigir cada um deles.

O "banco de dados" é simulado: cada consulta demora 100 ms e é contada. Assim os testes conseguem ver quantas vezes alguém foi ao banco.

## Preparando o ambiente

Você só precisa do **Docker** (com o Docker Compose). Não é preciso instalar Java, Maven nem Redis: tudo roda em containers.

```bash
# Rode o verificador uma vez (sobe o Redis, baixa as dependências e mostra o placar)
verificar.cmd        # Windows
./verificar.sh       # Linux / macOS
```

A primeira execução demora alguns minutos porque baixa as imagens e as dependências do Maven. Elas ficam guardadas em volumes do Docker, então das próximas vezes é rápido.

Se aparecer `PLACAR: 0 de 4 bugs corrigidos`, está tudo pronto. É assim mesmo: os bugs ainda estão lá.

## Como resolver

1. Leia a descrição dos bugs abaixo.
2. Encontre o código com problema em `src/main/java/br/ufla/desafiocache/service/`.
3. Corrija e rode o verificador de novo.
4. Repita até o placar mostrar `4 de 4 bugs corrigidos`.

Regras:

- **Não mexa nos testes** (`src/test`), no `BancoSimulado` nem no `Json`.
- Não vale "resolver" tirando o cache. Os testes também conferem que o cache continua funcionando.
- Um bug só conta como corrigido quando **todos** os testes dele passam.

## Os bugs

### Bug 1 · Cada um vê o próprio perfil

A Ana abre o perfil dela. Um minuto depois, o Bruno abre o dele e aparece o nome e o e-mail da Ana.

- Teste: `Bug1PerfilTest`
- Endpoint para ver na mão: `GET /perfil/1` e depois `GET /perfil/2`

```bash
curl http://localhost:8080/perfil/1
curl http://localhost:8080/perfil/2
# Com o bug, a segunda chamada também devolve {"id":1,"nome":"Ana",...}
```

### Bug 2 · O nome novo do evento aparece na lista

O organizador corrige o nome de um evento. O banco é atualizado, mas a lista de eventos continua mostrando o nome antigo por até 10 minutos.

- Teste: `Bug2EventoTest`
- Endpoint: `GET /eventos`, depois `PUT /eventos/1/nome?nome=Novo` e `GET /eventos` de novo

```bash
curl http://localhost:8080/eventos
curl -X PUT "http://localhost:8080/eventos/1/nome?nome=Novo%20Nome"
curl http://localhost:8080/eventos
# Com o bug, o evento 1 continua aparecendo como "Show de Rock"
```

### Bug 3 · Não vender mais ingressos do que existem

Na abertura de vendas, 40 processos tentam comprar 400 vezes ao mesmo tempo um evento com 100 ingressos. O sistema vende bem mais do que 100.

- Teste: `Bug3IngressoTest`
- Endpoints: `POST /ingressos/7/abrir?quantidade=100` e `POST /ingressos/7/comprar`

```bash
# Abre as vendas com 100 ingressos
curl -X POST "http://localhost:8080/ingressos/7/abrir?quantidade=100"

# Faz 400 compras, 40 de cada vez, e conta quantas deram certo
seq 400 | xargs -P 40 -I{} curl -s -X POST http://localhost:8080/ingressos/7/comprar -w '\n' | grep -c '"comprou":true'
# O certo é 100. Com o bug, o número passa bem de 100
```

### Bug 4 (bônus) · Stampede quando o ranking expira

O ranking é caro de calcular e fica 60 segundos no cache. Quando a chave expira, chegam 200 pedidos juntos e dezenas deles vão ao banco ao mesmo tempo para recalcular a mesma coisa. Só um deveria recalcular; os outros esperam.

- Teste: `Bug4RankingTest`
- Endpoint: `GET /ranking` e `GET /banco/consultas`

```bash
# Apaga a chave do ranking para simular que ela acabou de expirar
docker exec desafio-cache-redis redis-cli DEL ranking:geral

# Anota o total de consultas, faz 200 pedidos ao mesmo tempo e confere de novo
curl http://localhost:8080/banco/consultas
seq 200 | xargs -P 200 -I{} curl -s -o /dev/null http://localhost:8080/ranking
curl http://localhost:8080/banco/consultas
# O certo é o total subir 1 (no máximo 2). Com o bug, sobe dezenas ou centenas
```

Os comandos com `seq` e `xargs` funcionam no Linux, no macOS e, no Windows, no Git Bash ou no WSL. Suba a API antes com `docker compose up -d`.

## Testar no Postman

A pasta `postman/` tem a coleção `desafio-cache.postman_collection.json`. No Postman, use **Import** e selecione esse arquivo. A coleção tem uma pasta por bug com as chamadas acima, e a variável `baseUrl` (padrão `http://localhost:8080`) define o endereço da API.

Nos bugs 1 e 2, as requisições têm testes que conferem o comportamento correto: enquanto o bug existir, eles falham na aba **Test Results**. Os bugs 3 e 4 só aparecem com muitas chamadas simultâneas, e o Runner do Postman executa uma de cada vez; para eles, use os comandos `curl` acima.

## Comandos úteis

```bash
# Rodar só um bug
./verificar.sh -Dtest=Bug1PerfilTest       # Windows: verificar.cmd -Dtest=Bug1PerfilTest
# (equivale a: docker compose run --rm testes -Dtest=Bug1PerfilTest)

# Subir o Redis e a API em http://localhost:8080
docker compose up -d
docker compose logs -f api                 # acompanhar a API subindo (Ctrl+C para sair)

# Depois de editar o código, recompilar e reiniciar a API
docker compose restart api

# Olhar o que está no Redis
docker exec -it desafio-cache-redis redis-cli
> KEYS *
> GET perfil
> TTL eventos:ativos

# Desligar tudo
docker compose down
```

Os testes usam o banco lógico 1 do Redis (`SELECT 1` no `redis-cli`), e a API usa o 0.

Como funciona: o `docker-compose.yml` tem três serviços. `redis` é o cache, `api` roda `mvn spring-boot:run` e `testes` roda `mvn test`, os dois usando uma imagem com Java 17 e Maven. A pasta do projeto é montada dentro dos containers, então o que você edita em `src/` é usado na hora. O Redis não expõe porta na sua máquina; a API e os testes falam com ele pela rede interna do Docker.

## Problemas comuns

| Sintoma | Solução |
|---|---|
| `docker: command not found` ou `docker compose` não existe | Instale o Docker Desktop (Windows/macOS) ou Docker Engine + plugin Compose (Linux) |
| `Cannot connect to the Docker daemon` | Abra o Docker Desktop ou rode `sudo systemctl start docker` |
| Aviso de conexão com o Redis no placar | Rode `docker compose up -d redis` e confira com `docker ps` |
| Porta 8080 ocupada ao subir a API | Pare o que usa a 8080 ou troque `"8080:8080"` por `"8081:8080"` no `docker-compose.yml` |
| `verificar.sh` não executa no Linux/macOS | `chmod +x verificar.sh` |
| Uma pasta `target/` vazia apareceu no projeto | É normal: é o ponto de montagem do volume de build. Pode ignorar |

---

> A pasta `resolucao/` tem o gabarito, com o motivo de cada bug e a correção. Remova essa pasta antes de distribuir o projeto.
