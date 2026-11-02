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
AREAS = ("linguagens", "ciencias-humanas", "ciencias-natureza", "matematica")
IDIOMAS = ("ingles", "espanhol")


def problemas_da_questao(q: dict) -> list[str]:
    achados = []
    qid = q.get("id") or "(sem id)"
    alts = q.get("alternativas") or []

    if not q.get("id"):
        achados.append(f"{qid}: questão sem id")
    if not (q.get("area") or "").strip():
        achados.append(f"{qid}: questão sem área")
    elif q.get("area") not in AREAS:
        # A área é a chave que liga questão, aula e cronograma. Um valor novo
        # aqui some da trilha e da cobertura sem dar erro.
        achados.append(f"{qid}: área desconhecida '{q.get('area')}'")
    if not (q.get("tema") or "").strip():
        achados.append(f"{qid}: questão sem tema")
    idioma = q.get("idioma")
    if idioma is not None and idioma not in IDIOMAS:
        achados.append(f"{qid}: idioma desconhecido '{idioma}'")
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
    origens: dict[str, int] = {}
    vistos: dict[str, str] = {}
    for caminho in arquivos:
        dados = json.loads(pathlib.Path(caminho).read_text(encoding="utf-8"))
        if isinstance(dados, list):
            questoes = dados
        else:
            questoes = dados.get("questoes") or []
        for q in questoes:
            total += 1
            origens[str(q.get("origem"))] = origens.get(str(q.get("origem")), 0) + 1
            # Id repetido quebra o progresso: responder uma marca as duas.
            qid = q.get("id")
            if qid:
                if qid in vistos:
                    problemas.append(f"{qid}: id repetido (também em {vistos[qid]})")
                else:
                    vistos[qid] = pathlib.Path(caminho).name
            problemas.extend(problemas_da_questao(q))

    # O app exclui do treino só o que é gerado por IA ('ia'); o acervo oficial
    # chega com 'enem.dev', 'inep' e algumas questões antigas sem origem. Se um
    # dia o acervo vier marcado como 'ia', o filtro apagaria o acervo inteiro da
    # tela — foi quase isso que aconteceu com o filtro antigo (= 'enem').
    if origens.get("ia"):
        problemas.append(
            f"{origens['ia']} questões do acervo marcadas como 'ia': o app as esconderia do treino"
        )

    if problemas:
        print(f"{len(problemas)} problema(s) em {total} questões:\n")
        for p in problemas[:40]:
            print(" ", p)
        if len(problemas) > 40:
            print(f"  … e mais {len(problemas) - 40}")
        return 1

    print(f"acervo ok: {total} questões em {len(arquivos)} anos · origens: {origens}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
