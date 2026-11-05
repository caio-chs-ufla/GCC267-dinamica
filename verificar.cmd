@echo off
REM Roda os testes e mostra o placar no final.
cd /d "%~dp0"
call mvnw.cmd test
