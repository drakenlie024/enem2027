# ENEM 2027 — app de estudos

App Android nativo (Kotlin + Jetpack Compose) para estudar para o ENEM 2027.

## Decisões de arquitetura

| Tema | Decisão |
|---|---|
| Plataforma | Android nativo, Kotlin + Compose, Material 3 |
| Tema | Escuro premium (roxo `#7C6CFF` / verde `#3DDC97`) |
| Build | GitHub Actions → APK assinado, instalação direta |
| Backend | Serviço novo na VPS (FastAPI), container separado, schema `enem` no Postgres |
| IA | Gateway opencode-go, com a chave **só no servidor** |
| Conteúdo | Pipeline na VPS monta o acervo (fonte: C3SL) e publica JSON versionado; o app sincroniza |
| Estudo | Repetição espaçada (FSRS) sobre os temas escolhidos pelo usuário |
| Offline | Completo. Só correção de redação e sync exigem conexão |

Regras que não se quebram:
1. Nenhum segredo (chave de IA, token, keystore) dentro do APK ou no git.
2. Banco do app em schema separado — nada se mistura com dados do painel de clientes (CPF/LGPD).
3. Backup automático do Postgres é obrigatório.
4. Conteúdo sempre com origem marcada: `enem` (por ano) ou `ia`.

## Estrutura

```
app/src/main/java/com/franferreira/enem2027/
  MainActivity.kt          # shell com bottom nav
  ui/theme/                # cores, tipografia, tema
  ui/screens/TodayScreen.kt
```

## Build local

Requer JDK 17 e Android SDK (compileSdk 35).

```bash
./gradlew assembleDebug
```

## Build pelo CI

Push em `main` dispara `.github/workflows/build-apk.yml`, que publica o APK como
artifact do workflow. Para assinar com sua chave, cadastre nos secrets do GitHub:

- `RELEASE_STORE_FILE` — caminho do arquivo `.keystore` dentro do runner
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Sem esses secrets o CI gera o APK de debug, só para validar o build.
