#!/bin/sh
# Roda os testes e mostra o placar no final.
cd "$(dirname "$0")"
./mvnw test
