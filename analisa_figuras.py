import glob
import json
import pathlib
import re

# Questão que cita figura/imagem no enunciado ou contexto, mas não traz nenhuma
# imagem na questão nem nas alternativas: não tem como responder.
CITA = re.compile(
    r'\b(figura|imagem|ilustra[çc][ãa]o|charge|tirinha|quadrinho|gr[áa]fico|'
    r'esquema|mapa|tabela|fotografia|pintura|painel)\b',
    re.I,
)

sem_imagem = []
total = 0
for caminho in sorted(glob.glob('app/src/main/assets/enem/enem-*.json')):
    dados = json.loads(pathlib.Path(caminho).read_text(encoding='utf-8'))
    for q in dados.get('questoes', []):
        total += 1
        texto = f"{q.get('enunciado') or ''} {q.get('contexto') or ''}"
        if not CITA.search(texto):
            continue
        tem_q = bool(q.get('arquivos'))
        tem_alt = any(a.get('arquivo') for a in q.get('alternativas') or [])
        if not tem_q and not tem_alt:
            sem_imagem.append((q['id'], texto.strip()[:95]))

print('questoes no acervo:', total)
print('citam imagem e nao tem imagem nenhuma:', len(sem_imagem))
por_ano = {}
for qid, texto in sem_imagem:
    ano = qid.split('-')[1]
    por_ano[ano] = por_ano.get(ano, 0) + 1
print('por ano:', dict(sorted(por_ano.items())))
print()
for qid, texto in sem_imagem[:12]:
    print(qid, '|', texto.encode('ascii', 'backslashreplace').decode())
