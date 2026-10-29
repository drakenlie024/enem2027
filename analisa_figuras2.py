import glob
import json
import pathlib
import re

# Dependência FORTE: o texto manda olhar a imagem, ou fala dela como o objeto da
# questão. Aí sem figura não há resposta.
FORTE = re.compile(
    r'(a imagem|as imagens|a figura|as figuras|a ilustra[çc][ãa]o|a charge|a tirinha|'
    r'o gr[áa]fico|os gr[áa]ficos|o esquema|o mapa|a tabela|a fotografia|a pintura|'
    r'observe|observando|de acordo com a imagem|conforme a figura|na figura|'
    r'no gr[áa]fico|no esquema|no mapa|na tabela)',
    re.I,
)
# Falso positivo comum: a palavra aparece em sentido figurado.
FIGURADO = re.compile(r'figura her[óo]ica|figura de linguagem|figura p[úu]blica|figura hist[óo]rica', re.I)


def tem_imagem(q):
    return bool(q.get('arquivos')) or any(a.get('arquivo') for a in q.get('alternativas') or [])


fortes = []
for caminho in sorted(glob.glob('app/src/main/assets/enem/enem-*.json')):
    dados = json.loads(pathlib.Path(caminho).read_text(encoding='utf-8'))
    for q in dados.get('questoes', []):
        if tem_imagem(q):
            continue
        texto = f"{q.get('enunciado') or ''} {q.get('contexto') or ''}"
        if FIGURADO.search(texto):
            continue
        if FORTE.search(texto):
            fortes.append(q['id'])

print('sem imagem e dependentes dela:', len(fortes))
por_ano = {}
for qid in fortes:
    ano = qid.split('-')[1]
    por_ano[ano] = por_ano.get(ano, 0) + 1
print('por ano:', dict(sorted(por_ano.items())))
pathlib.Path('remover_figuras.txt').write_text('\n'.join(fortes), encoding='utf-8')
