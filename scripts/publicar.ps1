# Publica o APK na VPS em um comando.
#
# Antes isso era: abrir a API do GitHub, descobrir o run, achar o artifact,
# baixar o zip, extrair, achar o apk, calcular o hash, scp, escrever o
# versao.json, conferir o hash na VPS e no link público. Seis ou oito passos
# manuais que já falharam por detalhe.
#
# Uso:
#   .\scripts\publicar.ps1                 # último build verde da main
#   .\scripts\publicar.ps1 -RunId 123456   # um run específico
#   .\scripts\publicar.ps1 -WhatIf         # mostra o que faria, sem subir
#
# Requer: git (para o token), ssh/scp configurados para a VPS.

param(
    [string]$RunId,
    [string]$Host_ = "root@103.199.186.104",
    [string]$Chave = "$env:USERPROFILE\.ssh\id_ed25519",
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"
$repo = "drakenlie024/enem2027"
$raiz = Split-Path -Parent $PSScriptRoot

function Token {
    $entrada = "protocol=https`nhost=github.com`n`n"
    $saida = $entrada | git credential fill 2>&1
    ($saida | Select-String '^password=').Line -replace '^password=', ''
}

$token = Token
if (-not $token) { throw "não consegui obter o token do GitHub (git credential fill)" }

$cabecalho = @{ Authorization = "token $token"; "User-Agent" = "opencode" }

if (-not $RunId) {
    $runs = Invoke-RestMethod -Headers $cabecalho "https://api.github.com/repos/$repo/actions/runs?branch=main&per_page=10"
    $run = $runs.workflow_runs | Where-Object { $_.conclusion -eq "success" } | Select-Object -First 1
    if (-not $run) { throw "nenhum build verde encontrado na main" }
    $RunId = $run.id
} else {
    $run = Invoke-RestMethod -Headers $cabecalho "https://api.github.com/repos/$repo/actions/runs/$RunId"
}

Write-Host "run $RunId · commit $($run.head_sha.Substring(0,8)) · $($run.conclusion)"

$artefatos = Invoke-RestMethod -Headers $cabecalho "https://api.github.com/repos/$repo/actions/runs/$RunId/artifacts"
$artefato = $artefatos.artifacts | Select-Object -First 1
if (-not $artefato) { throw "o run $RunId não tem artifact" }

$temp = Join-Path $env:TEMP "publicar-enem"
Remove-Item -Recurse -Force $temp -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $temp | Out-Null

$zip = Join-Path $temp "apk.zip"
Invoke-WebRequest -Headers $cabecalho -Uri $artefato.archive_download_url -OutFile $zip
Expand-Archive -Path $zip -DestinationPath $temp -Force

$apk = Get-ChildItem $temp -Recurse -Filter "app-release.apk" | Select-Object -First 1
if (-not $apk) { throw "o artifact não tem app-release.apk (build saiu como debug?)" }

$hash = (Get-FileHash $apk.FullName -Algorithm SHA256).Hash.ToLower()

$gradle = Get-Content (Join-Path $raiz "app\build.gradle.kts") -Raw
$codigo = [regex]::Match($gradle, 'versionCode\s*=\s*(\d+)').Groups[1].Value
$nome = [regex]::Match($gradle, 'versionName\s*=\s*"([^"]+)"').Groups[1].Value
if (-not $codigo -or -not $nome) { throw "não achei versionCode/versionName no build.gradle.kts" }

Write-Host "versão $nome (código $codigo) · sha256 $($hash.Substring(0,16))…"

$manifesto = "{`"versionCode`":$codigo,`"versionName`":`"$nome`",`"url`":`"https://leticia.franferreira.cloud/leticia.apk`",`"sha256`":`"$hash`"}"
$arquivoManifesto = Join-Path $temp "versao.json"
[System.IO.File]::WriteAllText($arquivoManifesto, $manifesto)

if ($WhatIf) {
    Write-Host "WhatIf: subiria $($apk.FullName) e o versao.json para $Host_"
    return
}

$ssh = @("-o", "BatchMode=yes", "-i", $Chave)
scp @ssh $apk.FullName "${Host_}:/tmp/leticia-new.apk"
scp @ssh $arquivoManifesto "${Host_}:/tmp/versao-new.json"

# O backup guarda a versão anterior antes de sobrescrever.
$comando = @"
set -e
cp /opt/enem/apk/leticia.apk /opt/enem/apk/leticia-anterior-$nome.apk
mv /tmp/leticia-new.apk /opt/enem/apk/leticia.apk
mv /tmp/versao-new.json /opt/enem/apk/versao.json
sha256sum /opt/enem/apk/leticia.apk | cut -c1-16
curl -sL https://leticia.franferreira.cloud/leticia.apk | sha256sum | cut -c1-16
curl -sL https://leticia.franferreira.cloud/versao.json
"@
ssh @ssh $Host_ $comando

Write-Host ""
Write-Host "confira acima: os dois hashes têm que ser $($hash.Substring(0,16))"
