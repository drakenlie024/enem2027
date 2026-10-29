import json
import pathlib
import re
import time
import urllib.error
import urllib.request

IDS = [l.strip() for l in pathlib.Path('remover_figuras.txt').read_text(encoding='utf-8').splitlines() if l.strip()]
PASTA = pathlib.Path('app/src/main/assets/enem')


def buscar(ano: int, indice: int):
    url = f'https://api.enem.dev/v1/exams/{ano}/questions/{indice}'
    for tentativa in range(4):
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        try:
            with urllib.request.urlopen(req, timeout=25) as r:
                return json.loads(r.read().decode('utf-8'))
        except urllib.error.HTTPError as e:
            if e.code == 429 and tentativa < 3:
                time.sleep(15 * (tentativa + 1))
                continue
            return None
        except Exception:
            return None
    return None


removidas = []
recuperadas = []
sem_fonte = []

for qid in IDS:
    m = re.match(r'enem-(\d{4})-q(\d+)', qid)
    if not m:
        continue
    ano, indice = int(m.group(1)), int(m.group(2))

    if ano >= 2024:
        removidas.append(qid)   # a fonte não tem mais esses anos
        continue

    src = buscar(ano, indice)
    time.sleep(1.6)
    if not src:
        sem_fonte.append(qid)
        continue
    arquivos = src.get('files') or []
    alts = {a.get('letter'): a.get('file') for a in src.get('alternatives') or []}
    if arquivos or any(alts.values()):
        recuperadas.append((qid, arquivos, {k: v for k, v in alts.items() if v}))
    else:
        sem_fonte.append(qid)   # o texto citava imagem, mas a questão é só texto

print('remover (2024/2025):', len(removidas))
print('recuperar imagem:', len(recuperadas))
print('sem fonte e sem figura:', len(sem_fonte))

# --- aplica
for qid in removidas:
    ano = int(qid.split('-')[1])
    caminho = PASTA / f'enem-{ano}.json'
    dados = json.loads(caminho.read_text(encoding='utf-8'))
    dados['questoes'] = [q for q in dados['questoes'] if q['id'] != qid]
    caminho.write_text(json.dumps(dados, ensure_ascii=False, indent=2), encoding='utf-8')

# recuperação: injeta o que a fonte tem
por_ano = {}
for qid, arquivos, alts in recuperadas:
    ano = int(qid.split('-')[1])
    caminho = PASTA / f'enem-{ano}.json'
    if ano not in por_ano:
        por_ano[ano] = json.loads(caminho.read_text(encoding='utf-8'))
    dados = por_ano[ano]
    for q in dados['questoes']:
        if q['id'] != qid:
            continue
        if arquivos:
            q['arquivos'] = arquivos
        for a in q.get('alternativas') or []:
            if alts.get(a.get('letra')):
                a['arquivo'] = alts[a['letra']]

for ano, dados in por_ano.items():
    (PASTA / f'enem-{ano}.json').write_text(
        json.dumps(dados, ensure_ascii=False, indent=2), encoding='utf-8'
    )

pathlib.Path('relatorio_figuras.txt').write_text(
    'removidas:\n' + '\n'.join(removidas) +
    '\n\nrecuperadas:\n' + '\n'.join(q for q, _, _ in recuperadas) +
    '\n\nsem fonte:\n' + '\n'.join(sem_fonte),
    encoding='utf-8',
)
print('feito')
