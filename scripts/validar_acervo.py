"""Valida o acervo que vai dentro do APK.

Roda no CI antes dos testes. Existe porque duas vezes questão inválida chegou ao
aparelho: alternativas vazias (a Nathália não conseguia responder) e questão com
menos de cinco alternativas. Compilar não pega isso — só olhando os dados.

Sai com código 1 se achar problema, para o build parar antes de publicar.
"""
from __future__ import annotations

import glob
import json
import pathlib
import sys

PASTA = pathlib.Path(__file__).resolve().parents[1] / "app/src/main/assets/enem"
LETRAS = ("A", "B", "C", "D", "E")


def problemas_da_questao(q: dict) -> list[str]:
    achados = []
    qid = q.get("id") or "(sem id)"
    alts = q.get("alternativas") or []

    if not q.get("id"):
        achados.append(f"{qid}: questão sem id")
    if not q.get("area"):
        achados.append(f"{qid}: questão sem área")
    if not alts:
        achados.append(f"{qid}: questão sem alternativas")
        return achados

    letras = [a.get("letra") for a in alts]
    if len(alts) != len(set(letras)):
        achados.append(f"{qid}: letra repetida nas alternativas ({letras})")

    sem_texto = [
        a.get("letra")
        for a in alts
        if not (a.get("texto") or "").strip() and not a.get("arquivo")
    ]
    if len(sem_texto) == len(alts):
        achados.append(f"{qid}: nenhuma alternativa tem texto nem imagem")
    elif sem_texto:
        achados.append(f"{qid}: alternativa sem texto nem imagem ({sem_texto})")

    gabarito = (q.get("gabarito") or "").strip().upper()
    if gabarito not in letras:
        achados.append(f"{qid}: gabarito '{gabarito}' não está entre as alternativas")
    return achados


def main() -> int:
    arquivos = sorted(glob.glob(str(PASTA / "enem-*.json")))
    if not arquivos:
        print(f"acervo não encontrado em {PASTA}")
        return 1

    problemas: list[str] = []
    total = 0
    for caminho in arquivos:
        dados = json.loads(pathlib.Path(caminho).read_text(encoding="utf-8"))
        if isinstance(dados, list):
            questoes = dados
        else:
            questoes = dados.get("questoes") or []
        for q in questoes:
            total += 1
            problemas.extend(problemas_da_questao(q))

    if problemas:
        print(f"{len(problemas)} problema(s) em {total} questões:\n")
        for p in problemas[:40]:
            print(" ", p)
        if len(problemas) > 40:
            print(f"  … e mais {len(problemas) - 40}")
        return 1

    print(f"acervo ok: {total} questões em {len(arquivos)} anos")
    return 0


if __name__ == "__main__":
    sys.exit(main())
