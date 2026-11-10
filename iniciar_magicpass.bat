@echo off
cd /d "C:\projeto integrador\magicpass"
echo Iniciando MagicPass (backend) na porta 8081...
start "MagicPass Backend" cmd /k java -jar magicpass.jar --server.port=8081
timeout /t 8 /nobreak
start http://localhost:8081
