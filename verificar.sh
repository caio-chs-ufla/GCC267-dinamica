#!/bin/sh
# Roda os testes dentro do Docker e mostra o placar no final.
# Não precisa de Java instalado: só do Docker.
cd "$(dirname "$0")"
docker compose run --rm testes "$@"
