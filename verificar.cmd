@echo off
REM Roda os testes dentro do Docker e mostra o placar no final.
REM Nao precisa de Java instalado: so do Docker.
cd /d "%~dp0"
docker compose run --rm testes %*
