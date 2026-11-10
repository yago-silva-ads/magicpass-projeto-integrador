@echo off
cd /d "C:\projeto integrador\magicpass"
echo Iniciando MagicPass (backend)...
start "MagicPass Backend" cmd /k java -jar magicpass.jar
timeout /t 8 /nobreak
start http://localhost:8080
