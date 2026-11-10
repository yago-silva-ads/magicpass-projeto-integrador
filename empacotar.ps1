# Gera UM unico arquivo executavel (backend + front Angular juntos) e o .zip para o BlackBoard.
# Uso: powershell -ExecutionPolicy Bypass -File empacotar.ps1
$ErrorActionPreference = "Stop"
$raiz = $PSScriptRoot

Write-Host "1/3 Build do Angular..."
Push-Location "$raiz\frontend"
if (-not (Test-Path node_modules)) { npm install }
npx ng build
Pop-Location

Write-Host "2/3 Copiando o front para dentro do Spring Boot e gerando o .jar..."
$static = "$raiz\backend\src\main\resources\static"
if (Test-Path $static) { Remove-Item $static -Recurse -Force }
Copy-Item "$raiz\frontend\dist\frontend\browser" $static -Recurse
Push-Location "$raiz\backend"
mvn -q -B -DskipTests package
Pop-Location
Copy-Item "$raiz\backend\target\magicpass-1.0.0.jar" "$raiz\magicpass.jar" -Force

Write-Host "3/3 Gerando o zip de entrega..."
$zip = "$raiz\..\MagicPass_ProjetoIntegrador2026.zip"
if (Test-Path $zip) { Remove-Item $zip -Force }
$itens = @("$raiz\README.md", "$raiz\INTEGRANTES.md", "$raiz\magicpass.jar", "$raiz\empacotar.ps1", "$raiz\backend\pom.xml", "$raiz\backend\src",
           "$raiz\frontend\src", "$raiz\frontend\public", "$raiz\frontend\package.json", "$raiz\frontend\package-lock.json",
           "$raiz\frontend\angular.json", "$raiz\frontend\tsconfig.json", "$raiz\frontend\tsconfig.app.json", "$raiz\frontend\proxy.conf.json")
$tmp = Join-Path $env:TEMP "magicpass_zip"
if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
New-Item -ItemType Directory $tmp | Out-Null
foreach ($i in $itens) {
  $rel = $i.Substring($raiz.Length + 1)
  $dest = Join-Path $tmp $rel
  New-Item -ItemType Directory -Force (Split-Path $dest) | Out-Null
  Copy-Item $i $dest -Recurse
}
Compress-Archive -Path "$tmp\*" -DestinationPath $zip
Write-Host "Pronto! Rode com:  java -jar magicpass.jar   e abra http://localhost:8080"
Write-Host "Zip: $zip"
