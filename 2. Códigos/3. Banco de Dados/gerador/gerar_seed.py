"""
LogiCup — Gerador do seed de TODAS as fases a partir do Banco de Questões Mestre (.docx).

POR QUE ESTE SCRIPT EXISTE
  O Banco de Questões (BancoDeQuestoesLogiCup_v3_0.docx) é a fonte ÚNICA da verdade do conteúdo.
  Em vez de copiar 110 questões à mão para o SQL (lento e sujeito a erro de digitação), este
  script LÊ o documento, monta os dados de cada fase/rodada/questão, CONFERE tudo e só então
  grava o SQL. Se o documento mudar, basta rodar de novo.

COMO RODAR (na pasta gerador/):
  pip install python-docx
  python gerar_seed.py "../../../4. Documentos LogiCup/BancoDeQuestoesLogiCup_v3_0.docx"

O QUE ELE GERA:
  ../02_seed_conteudo.sql -> INSERTs de modulo, rodada, exercicio e dica (banco recém-criado, VAZIO)
  seed_revisao.json       -> os mesmos dados em JSON legível, para revisão humana (não vai para o
                             banco nem para o GitHub)

COMO ELE LÊ O DOCUMENTO (estrutura esperada do .docx):
  Título 1  "FASE n — NOME"                       -> uma fase (tabela modulo)
  Título 2  "Rodada n — título"                   -> uma rodada (tabela rodada)
            parágrafo "Objetivo da rodada: ..."   -> conteudo_teorico.objetivos
            parágrafo "Questões: F1.Q2, ... · ..."-> ORDEM das questões na rodada (conferida)
  Título 3  "1.1 Título da seção"                 -> uma seção de teoria
  Título 4  "F1.Q2 — Título"                      -> uma questão (tabela exercicio)
            parágrafos "Rótulo: valor" (Tipo, Habilidade, Enunciado, Opções, Resposta esperada...)
  Código Java = parágrafo inteiro na fonte Consolas.

REGRA DE OURO: na dúvida, o script PARA com uma mensagem dizendo qual questão e por quê.
  Nenhum .sql é gravado se alguma conferência falhar.

O que o documento NÃO diz (ex.: respostas equivalentes aceitas numa lacuna) fica em
ajustes_seed.py, separado e comentado, para ficar claro o que é "do documento" e o que é "nosso".
"""
import json
import random
import re
import sys
import unicodedata
from pathlib import Path

import docx
from docx.oxml.ns import qn
from docx.table import Table
from docx.text.paragraph import Paragraph

from ajustes_seed import AJUSTES, PARAGRAFOS_IGNORADOS_NA_TEORIA

sys.path.insert(0, str(Path(__file__).resolve().parent / "codigo"))
import aplicar_codigo  # noqa: E402  Sprint 9 (ADR-001): as 9 questões de código viram formato CODIGO

PASTA = Path(__file__).resolve().parent          # gerador/
PASTA_SQL = PASTA.parent                         # "3. Banco de Dados/", onde ficam os .sql

# ---------------------------------------------------------------------------
# Regras fixas do projeto (decisões já tomadas) — usadas nas CONFERÊNCIAS
# ---------------------------------------------------------------------------
XP_POR_NUMERO = {1: 10, 2: 10, 3: 15, 4: 15, 5: 15, 6: 20, 7: 20, 8: 25, 9: 25, 10: 50}       # Fases 1 a 9
XP_POR_NUMERO_F10 = {1: 15, 2: 15, 3: 20, 4: 20, 5: 20, 6: 25, 7: 25, 8: 30, 9: 30, 10: 150}
XP_ESPERADO_FASE = {**{f: 205 for f in range(1, 9)}, 9: 410, 10: 350}
NOMES_PROIBIDOS = ["Joana", "João", "Ana ", "Carlos", "Pedro", "Maria "]  # regra: personagem = Gattuccino

TIPOS = {  # rótulo do documento -> enum TipoExercicio (backend)
    "Verdadeiro ou Falso": "VERDADEIRO_FALSO",
    "Quiz de Múltipla Escolha": "MULTIPLA_ESCOLHA",
    "Blocos de Programação": "BLOCOS",
    "Completar Código": "COMPLETAR_CODIGO",
    "Encontrar Erro": "ENCONTRAR_ERRO",
    "Prever a Saída": "PREVER_SAIDA",
    "Escrever Código": "ESCREVER_CODIGO",
    "Associação": "ASSOCIACAO",
    "Desafio Relâmpago": "DESAFIO_RELAMPAGO",
    "Puzzle Lógico": "PUZZLE_LOGICO",
    "Ordem Correta": "ORDEM_CORRETA",
    "Boss Challenge": "BOSS_CHALLENGE",
    "Boss Final": "BOSS_CHALLENGE",
}
FORMATOS = {"ESCOLHA_UNICA", "ORDENACAO", "ASSOCIACAO", "RESPOSTA_CURTA", "TEXTO_LIVRE"}

# Rótulos dos campos de cada questão, na forma "Rótulo: valor"
ROTULOS = ["Objetivo pedagógico", "Habilidade", "Enunciado", "Opções", "Conceito/conteúdo trabalhado",
           "Resposta esperada", "Critério de correção (palavras-chave)", "Critério de correção",
           "Explicação da resposta", "Dica"]


ERROS = []  # erros de questões, acumulados


class ErroSeed(Exception):
    """Erro de conteúdo: o documento não está no formato esperado ou uma conferência falhou."""


def falhar(onde, msg):
    raise ErroSeed(f"[{onde}] {msg}")


# ---------------------------------------------------------------------------
# 1) Leitura do .docx -> lista simples de blocos {tipo, texto}
# ---------------------------------------------------------------------------
def ler_blocos(caminho):
    """
    Transforma o .docx numa lista de blocos, na ordem do documento:
      H1..H4  títulos | P parágrafo | LI item de lista | CODE código (Consolas) | TABELA | IMG imagem
    Só olhamos o que importa para o seed; a formatação (negrito etc.) é descartada.
    """
    documento = docx.Document(caminho)
    blocos = []
    for el in documento.element.body.iterchildren():
        if el.tag == qn("w:tbl"):
            tabela = Table(el, documento)
            linhas = [[celula.text.strip() for celula in linha.cells] for linha in tabela.rows]
            blocos.append({"tipo": "TABELA", "linhas": linhas, "texto": ""})
            continue
        if el.tag != qn("w:p"):
            continue
        p = Paragraph(el, documento)
        texto = p.text.replace(" ", " ").rstrip()
        estilo = p.style.name if p.style is not None else ""
        if el.xpath(".//w:drawing") or el.xpath(".//w:pict"):
            blocos.append({"tipo": "IMG", "texto": texto})
            continue
        if estilo.startswith("Heading "):
            blocos.append({"tipo": "H" + estilo.split()[-1], "texto": texto.strip()})
            continue
        if not texto.strip():
            continue
        runs = [r for r in p.runs if r.text.strip()]
        codigo = bool(runs) and all(r.font.name == "Consolas" for r in runs)
        lista = el.pPr is not None and el.pPr.numPr is not None
        tipo = "CODE" if codigo and not lista else ("LI" if lista else "P")
        blocos.append({"tipo": tipo, "texto": texto, "codigo": codigo})
    return blocos


# ---------------------------------------------------------------------------
# 2) Recorte em fases -> rodadas -> (teoria, questões)
# ---------------------------------------------------------------------------
RE_FASE = re.compile(r"^FASE (\d+) — (.+)$")
RE_RODADA = re.compile(r"^Rodada (\d+) — (.+)$")
RE_SECAO = re.compile(r"^(\d+)\.(\d+)\s+(.+)$")
RE_QUESTAO = re.compile(r"^(F\d+(?:\.R\d)?\.Q\d+) — (.+)$")


def titulos_das_fases(blocos):
    """Nome 'bonito' de cada fase, tirado da tabela 'Status da revisão' (o Título 1 é todo em MAIÚSCULAS)."""
    for b in blocos:
        if b["tipo"] == "TABELA" and b["linhas"] and b["linhas"][0][0].startswith("Fase"):
            nomes = {}
            for linha in b["linhas"][1:]:
                m = re.match(r"^(\d+) — (.+)$", linha[0])
                if m:
                    nomes[int(m.group(1))] = m.group(2).strip()
            return nomes
    falhar("documento", "tabela 'Status da revisão' não encontrada")


def recortar(blocos):
    nomes = titulos_das_fases(blocos)
    fases = []
    fase = rodada = secao = questao = None
    zona = None  # 'ementa' | 'rodada' | 'teoria' | 'questoes' | 'fora'
    for b in blocos:
        t, texto = b["tipo"], b["texto"]
        if t == "H1":
            m = RE_FASE.match(texto)
            fase = None if not m else {"ordem": int(m.group(1)), "titulo": nomes[int(m.group(1))],
                                       "ementa": [], "rodadas": []}
            if fase:
                fases.append(fase)
            rodada = secao = questao = None
            zona = None
            continue
        if fase is None:
            continue
        if t == "H2":
            questao = secao = None
            m = RE_RODADA.match(texto)
            if m:
                rodada = {"ordem": int(m.group(1)), "titulo": m.group(2).strip(), "cabecalho": [],
                          "secoes": [], "questoes": []}
                fase["rodadas"].append(rodada)
                zona = "rodada"
            else:
                rodada = None
                zona = "ementa" if texto.startswith("Ementa") else "fora"
            continue
        if t == "H3":
            questao = None
            if rodada is None:
                zona = "fora"
                continue
            if texto.startswith("Conteúdo teórico"):
                zona = "teoria"
            elif texto.startswith("Questões da Rodada"):
                zona = "questoes"
            else:
                m = RE_SECAO.match(texto)
                if zona in ("teoria",) and m:
                    secao = {"titulo": m.group(3).strip(), "blocos": []}
                    rodada["secoes"].append(secao)
                else:
                    zona = "fora"  # ex.: "Projeto integrador" depois das questões da Fase 10
                    rodada = None
            continue
        if t == "H4":
            m = RE_QUESTAO.match(texto)
            if zona != "questoes" or not m:
                if m:
                    falhar(texto, "questão fora da seção 'Questões da Rodada'")
                continue
            questao = {"codigo": m.group(1), "titulo": m.group(2).strip(), "blocos": []}
            rodada["questoes"].append(questao)
            continue
        # conteúdo
        if zona == "ementa":
            fase["ementa"].append(b)
        elif zona == "rodada" and rodada is not None:
            rodada["cabecalho"].append(b)
        elif zona == "teoria" and secao is not None:
            secao["blocos"].append(b)
        elif zona == "questoes" and questao is not None:
            questao["blocos"].append(b)
    return fases


# ---------------------------------------------------------------------------
# 3) Utilidades de texto
# ---------------------------------------------------------------------------
def normalizar(texto):
    """Para COMPARAR textos (resposta esperada x opção): sem acento, minúsculo, sem aspas/ponto final."""
    t = unicodedata.normalize("NFKD", texto)
    t = "".join(c for c in t if not unicodedata.combining(c)).lower()
    t = t.replace("“", "").replace("”", "").replace('"', "").replace("‘", "").replace("’", "")
    t = re.sub(r"\s+", " ", t).strip()
    return t.rstrip(".;").strip()


def separar_rotulo(texto):
    """'Habilidade: blá' -> ('Habilidade', 'blá'). Devolve (None, texto) se não começar com um rótulo."""
    for rotulo in ROTULOS:
        if texto.startswith(rotulo + ":"):
            return rotulo, texto[len(rotulo) + 1:].strip()
    return None, texto


# ---------------------------------------------------------------------------
# 4) Rodada: objetivo, ordem declarada das questões e teoria
# ---------------------------------------------------------------------------
def montar_teoria(rodada, onde):
    """
    conteudo_teorico, no formato que o frontend já entende, com UM acréscimo compatível:
      {"objetivos": [...],
       "teoria": [{"titulo", "texto", "blocos": [...]}],
       "exemplos": []}
    - "texto": a seção inteira em texto simples (é o que a tela ATUAL mostra);
    - "blocos": a mesma seção estruturada (parágrafo, lista, código, tabela), para a tela nova
      mostrar código em fonte monoespaçada e listas/tabelas formatadas.
    Os exemplos do documento ficam DENTRO das seções (como no documento), por isso "exemplos" é vazio.
    """
    objetivos = []
    ordem_declarada = None
    lendo_lista_objetivos = False
    for b in rodada["cabecalho"]:
        texto = b["texto"]
        if texto.startswith("Objetivo da rodada:"):        # Fases 1-8 e 10: uma frase
            objetivos.append(texto.split(":", 1)[1].strip())
        elif texto.startswith("Objetivos da rodada:"):     # Fase 9: lista logo abaixo
            lendo_lista_objetivos = True
            continue
        elif lendo_lista_objetivos and b["tipo"] == "LI":
            objetivos.append(texto.strip())
            continue
        lendo_lista_objetivos = False
        if texto.startswith("Questões:"):
            trecho = texto.split(":", 1)[1].split("·")[0]
            trecho = trecho.replace("(Boss Final)", "").replace("(Boss)", "").strip()
            intervalo = re.match(r"^(F\d+(?:\.R\d)?\.Q)(\d+) a \1(\d+)$", trecho)
            if intervalo:  # "F9.R1.Q1 a F9.R1.Q10"
                prefixo, ini, fim = intervalo.group(1), int(intervalo.group(2)), int(intervalo.group(3))
                ordem_declarada = [f"{prefixo}{n}" for n in range(ini, fim + 1)]
            else:
                ordem_declarada = [c.strip() for c in trecho.split(",")]
    if not objetivos:
        falhar(onde, "rodada sem 'Objetivo(s) da rodada:'")
    if not ordem_declarada:
        falhar(onde, "rodada sem a linha 'Questões: ...'")
    objetivos = [o[0].upper() + o[1:] for o in objetivos]

    secoes = []
    for s in rodada["secoes"]:
        estrutura, linhas_texto = [], []
        for b in s["blocos"]:
            if b["tipo"] == "IMG" or b["texto"] in PARAGRAFOS_IGNORADOS_NA_TEORIA:
                continue
            if b["tipo"] == "LI":
                if estrutura and estrutura[-1]["tipo"] == "lista":
                    estrutura[-1]["itens"].append(b["texto"])
                else:
                    estrutura.append({"tipo": "lista", "itens": [b["texto"]]})
                linhas_texto.append("- " + b["texto"])
            elif b["tipo"] == "CODE":
                estrutura.append({"tipo": "codigo", "codigo": b["texto"]})
                linhas_texto.append(b["texto"])
            elif b["tipo"] == "TABELA":
                estrutura.append({"tipo": "tabela", "linhas": b["linhas"]})
                linhas_texto.extend(" | ".join(linha) for linha in b["linhas"])
            else:
                estrutura.append({"tipo": "paragrafo", "texto": b["texto"]})
                linhas_texto.append(b["texto"])
        if not estrutura:
            falhar(onde, f"seção de teoria vazia: {s['titulo']}")
        secoes.append({"titulo": s["titulo"], "texto": "\n".join(linhas_texto), "blocos": estrutura})
    if not secoes:
        falhar(onde, "rodada sem teoria")
    return {"objetivos": objetivos, "teoria": secoes, "exemplos": []}, ordem_declarada


# ---------------------------------------------------------------------------
# 5) Questão
# ---------------------------------------------------------------------------
RE_CABECALHO = re.compile(r"^Tipo: (.+?) · Formato de resposta: (\S+)(?: \((.+?)\))? · Pontuação: (\d+) XP$")


def campos_da_questao(q):
    """Agrupa os blocos da questão por rótulo: {'Enunciado': [blocos...], ...} + o cabeçalho."""
    if not q["blocos"]:
        falhar(q["codigo"], "questão vazia")
        return None
    m = RE_CABECALHO.match(q["blocos"][0]["texto"])
    if not m:
        falhar(q["codigo"], f"cabeçalho Tipo/Formato/Pontuação inesperado: {q['blocos'][0]['texto']!r}")
    tipo_doc, formato, obs_formato, xp = m.group(1), m.group(2), m.group(3), int(m.group(4))
    campos, atual = {}, None
    for b in q["blocos"][1:]:
        rotulo, resto = separar_rotulo(b["texto"]) if b["tipo"] in ("P", "CODE") else (None, None)
        if rotulo:
            atual = rotulo
            campos[atual] = []
            if resto:
                campos[atual].append({**b, "texto": resto, "tipo": "P"})
        elif atual is None:
            falhar(q["codigo"], f"conteúdo antes do primeiro rótulo: {b['texto'][:60]!r}")
        else:
            campos[atual].append(b)
    return tipo_doc, formato, obs_formato, xp, campos


def texto_simples(blocos):
    """Junta blocos em texto (listas com '- '); código entra como está."""
    partes = []
    for b in blocos:
        if b["tipo"] == "LI":
            partes.append("- " + b["texto"])
        elif b["tipo"] != "IMG":
            partes.append(b["texto"])
    return "\n".join(partes).strip()


def unico_texto(campos, rotulo, onde, obrigatorio=True):
    blocos = campos.get(rotulo)
    if not blocos:
        if obrigatorio:
            falhar(onde, f"campo '{rotulo}' ausente")
        return None
    return texto_simples(blocos)


def juntar_codigo(partes):
    """Blocos de código separados por linha em branco; o comentário "// ..." gruda no código seguinte."""
    saida = ""
    for parte in partes:
        if saida:
            saida += "\n" if saida.endswith(":") and saida.splitlines()[-1].startswith("//") else "\n\n"
        saida += parte
    return saida


def ids_letras(n):
    return [chr(ord("a") + i) for i in range(n)]


def montar_questao(q, fase_ordem, rodada_ordem, posicao):
    onde = q["codigo"]
    tipo_doc, formato, obs_formato, xp, campos = campos_da_questao(q)
    ajuste = AJUSTES.get(onde, {})

    if tipo_doc not in TIPOS:
        falhar(onde, f"tipo desconhecido: {tipo_doc!r}")
    if formato not in FORMATOS:
        falhar(onde, f"formato desconhecido: {formato!r}")
    if obs_formato and "lacunas" in obs_formato and "lacunas" not in ajuste:
        falhar(onde, f"formato com observação '{obs_formato}' precisa de ajuste em ajustes_seed.py")

    # Enunciado: 1º parágrafo + parágrafos/itens de texto; o CÓDIGO vai para codigo_apoio
    blocos_enunciado = campos.get("Enunciado") or falhar(onde, "sem enunciado")
    # Texto ENTRE dois códigos (ex.: "Uso no programa:") vira comentário Java dentro do codigo_apoio,
    # para não ficar solto no fim do enunciado, longe do código a que se refere.
    pos_codigos = [i for i, b in enumerate(blocos_enunciado) if b["tipo"] == "CODE"]
    entre = set()
    if len(pos_codigos) >= 2:
        entre = {i for i in range(pos_codigos[0] + 1, pos_codigos[-1]) if blocos_enunciado[i]["tipo"] == "P"}
    texto_enunciado = [b for i, b in enumerate(blocos_enunciado) if b["tipo"] != "CODE" and i not in entre]
    codigo = []
    for i, b in enumerate(blocos_enunciado):
        if b["tipo"] == "CODE":
            codigo.append(b["texto"])
        elif i in entre:
            codigo.append("// " + b["texto"])

    habilidade = unico_texto(campos, "Habilidade", onde)
    explicacao = unico_texto(campos, "Explicação da resposta", onde)
    resposta = unico_texto(campos, "Resposta esperada", onde)
    dica = unico_texto(campos, "Dica", onde, obrigatorio=False)

    alternativas = gabarito = None
    if formato == "ESCOLHA_UNICA":
        opcoes = [b["texto"] for b in campos.get("Opções", []) if b["tipo"] in ("LI", "CODE", "P")
                  and not b["texto"].startswith("(Na plataforma")]
        if not opcoes:
            if tipo_doc != "Verdadeiro ou Falso":
                falhar(onde, "ESCOLHA_UNICA sem opções")
            opcoes = ["Verdadeiro", "Falso"]
        if ajuste.get("uma_linha_por_item"):
            opcoes = [o.replace(" / ", "\n") for o in opcoes]
        alvo = normalizar(resposta.replace(" / ", "\n") if ajuste.get("uma_linha_por_item") else resposta)
        if fase_ordem >= 4 and tipo_doc != "Verdadeiro ou Falso":
            # Nas Fases 4 a 10 o documento põe a resposta certa quase sempre como 1ª opção. O frontend
            # mostra as alternativas NA ORDEM DO BANCO, então embaralhamos aqui. A semente é o código da
            # questão: rodar o gerador de novo dá SEMPRE a mesma ordem (o SQL não muda à toa).
            # Fases 1 a 3 (revisadas pela Maria) já têm as respostas distribuídas: mantém a ordem do documento.
            random.Random(onde).shuffle(opcoes)
        ids = ids_letras(len(opcoes))
        certas = [i for i, o in zip(ids, opcoes) if normalizar(o) == alvo]
        if len(certas) != 1:
            falhar(onde, f"'Resposta esperada' corresponde a {len(certas)} opções (precisa ser 1): {resposta!r}")
        if len(set(normalizar(o) for o in opcoes)) != len(opcoes):
            falhar(onde, "opções repetidas")
        alternativas = [{"id": i, "texto": o} for i, o in zip(ids, opcoes)]
        icones = ajuste.get("icones")  # S7: símbolo de fluxograma ao lado do texto (ex.: F1.Q8)
        if icones:
            for alternativa in alternativas:
                if alternativa["texto"] not in icones:
                    falhar(onde, f"alternativa sem ícone definido nos ajustes: {alternativa['texto']!r}")
                alternativa["icone"] = icones[alternativa["texto"]]
        gabarito = {"correta": certas[0]}

    elif formato == "ORDENACAO":
        # itens: o que vem depois do 1º parágrafo do enunciado (ou no campo Opções)
        fonte = campos.get("Opções") or texto_enunciado[1:]
        itens = [b["texto"] for b in fonte]
        texto_enunciado = texto_enunciado[:1] if not campos.get("Opções") else texto_enunciado
        codigo = []  # na ordenação, as linhas de código SÃO os itens
        if len(itens) < 3:
            falhar(onde, f"ORDENACAO com poucos itens ({len(itens)})")
        ids = ids_letras(len(itens))
        bloco_resp = campos["Resposta esperada"]
        if any(b["tipo"] == "CODE" for b in bloco_resp):  # resposta escrita como o programa montado
            linhas = "\n".join(b["texto"] for b in bloco_resp if b["tipo"] == "CODE").split("\n")
            sequencia = [linha.strip() for linha in linhas if linha.strip()]
        else:
            sequencia = [s.strip() for s in resposta.split("→")]
        livres = list(zip(ids, itens))
        ordem = []
        for passo in sequencia:
            achou = next((par for par in livres if normalizar(par[1]) == normalizar(passo)
                          or normalizar(par[1]).startswith(normalizar(passo) + " ")
                          or normalizar(par[1]).rstrip(" {") == normalizar(passo)), None)
            if achou is None:
                falhar(onde, f"passo da resposta não encontrado entre os itens: {passo!r}")
            ordem.append(achou[0])
            livres.remove(achou)
        if livres:
            falhar(onde, f"itens que não aparecem na resposta: {[t for _, t in livres]}")
        alternativas = [{"id": i, "texto": t} for i, t in zip(ids, itens)]
        gabarito = {"ordem": ordem}

    elif formato == "ASSOCIACAO":
        # O documento escreve a associação de 3 jeitos; todos viram itens (1, 2, 3...) e opções (a, b, c...):
        #   (1) itens em lista + "Opções: A) ...; B) ..." (ou sem letras);
        #   (2) tabela de 2 colunas: itens à esquerda, opções à direita;
        #   (3) dois parágrafos: "1) ...; 2) ..." e "A) ...; B) ...".
        def sem_prefixo(texto, padrao):
            return re.sub(padrao, "", texto.strip()).strip().rstrip(".;").strip()

        def dividir(texto, padrao_inicio):
            partes = re.split(r";\s*(?=" + padrao_inicio + ")", texto.strip().rstrip("."))
            return [p for p in partes if p.strip()]

        LETRA, NUMERO = r"[A-Z][\)\.]\s", r"\d+[\)\.]\s"
        tabelas = [b for b in blocos_enunciado if b["tipo"] == "TABELA"]
        paragrafos_extras = [b for b in blocos_enunciado[1:] if b["tipo"] == "P"]
        if tabelas:
            linhas = tabelas[0]["linhas"]
            itens = [sem_prefixo(linha[0], r"^" + NUMERO) for linha in linhas]
            opcoes = [sem_prefixo(linha[1], r"^" + LETRA) for linha in linhas]
            texto_enunciado = texto_enunciado[:1]
        elif not any(b["tipo"] == "LI" for b in blocos_enunciado) and len(paragrafos_extras) == 2:
            itens = [sem_prefixo(p, r"^" + NUMERO) for p in dividir(paragrafos_extras[0]["texto"], NUMERO)]
            opcoes = [sem_prefixo(p, r"^" + LETRA) for p in dividir(paragrafos_extras[1]["texto"], LETRA)]
            texto_enunciado = texto_enunciado[:1]
        else:
            itens = [b["texto"] for b in blocos_enunciado if b["tipo"] == "LI"]
            texto_enunciado = [b for b in texto_enunciado if b["tipo"] != "LI"]
            bruto_opcoes = campos.get("Opções") or falhar(onde, "ASSOCIACAO sem 'Opções:'")
            if len(bruto_opcoes) == 1:
                texto_op = bruto_opcoes[0]["texto"]
                if re.match(LETRA, texto_op):
                    opcoes = [sem_prefixo(p, r"^" + LETRA) for p in dividir(texto_op, LETRA)]
                else:
                    opcoes = [o.strip().rstrip(".") for o in texto_op.split(";")]
            else:
                opcoes = [sem_prefixo(b["texto"], r"^" + LETRA) for b in bruto_opcoes]
        if len(itens) < 2 or len(opcoes) < 2:
            falhar(onde, "ASSOCIACAO sem itens/opções suficientes")
        if len(set(map(normalizar, opcoes))) != len(opcoes):
            falhar(onde, f"opções repetidas na associação: {opcoes}")
        ids_itens = [str(i + 1) for i in range(len(itens))]
        ids_opcoes = ids_letras(len(opcoes))
        pares = {}
        if re.match(r"^\s*\d+\s*-\s*[A-Z]\b", resposta):        # "1-B, 2-C, 3-A"
            for parte in resposta.rstrip(".").split(","):
                m = re.match(r"^\s*(\d+)\s*-\s*([A-Z])\s*$", parte)
                if not m:
                    falhar(onde, f"resposta de associação fora do padrão 'n-L': {parte!r}")
                letra = m.group(2).lower()
                if letra not in ids_opcoes:
                    falhar(onde, f"letra {m.group(2)} sem opção correspondente")
                pares[m.group(1)] = letra
        else:                                                 # "1 → Decomposição; 2 → ..."
            for parte in resposta.rstrip(".").split(";"):
                m = re.match(r"^\s*(\d+)\s*→\s*(.+?)\s*$", parte)
                if not m:
                    falhar(onde, f"resposta de associação fora do padrão 'n → opção': {parte!r}")
                alvo = normalizar(m.group(2))
                idx = [i for i, o in zip(ids_opcoes, opcoes) if normalizar(o) == alvo]
                if len(idx) != 1:
                    falhar(onde, f"opção da associação não encontrada: {m.group(2)!r}")
                pares[m.group(1)] = idx[0]
        if sorted(pares) != sorted(ids_itens):
            falhar(onde, f"associação não cobre todos os itens: {pares}")
        alternativas = {"itens": [{"id": i, "texto": t} for i, t in zip(ids_itens, itens)],
                        "opcoes": [{"id": i, "texto": t} for i, t in zip(ids_opcoes, opcoes)]}
        gabarito = {"pares": pares}

    elif formato == "RESPOSTA_CURTA":
        if "lacunas" in ajuste:
            # Sprint 9 (D3): várias lacunas. Público: só QUANTAS são; secreto: as aceitas de cada uma.
            lacunas = ajuste["lacunas"]
            alternativas = {"lacunas": len(lacunas)}
            gabarito = {"lacunas": [{"aceitas": a} for a in lacunas],
                        "diferenciarMaiusculas": ajuste.get("diferenciarMaiusculas", True)}
        else:
            if "aceitas" in ajuste:   # o documento explica a resposta em texto; a lista vem do ajuste
                aceitas = list(ajuste["aceitas"])
            else:
                aceitas = [resposta.strip().strip("“”\"").rstrip(".")]
                if "(" in aceitas[0] or " ou " in aceitas[0]:
                    falhar(onde, f"resposta com explicação/alternativas ({aceitas[0]!r}): defina 'aceitas' nos ajustes")
            aceitas += ajuste.get("aceitas_extras", [])
            gabarito = {"aceitas": aceitas, "diferenciarMaiusculas": ajuste.get("diferenciarMaiusculas", True)}

    elif formato == "TEXTO_LIVRE":
        criterio = unico_texto(campos, "Critério de correção (palavras-chave)", onde)
        m = re.match(r"^grupos? (.+); mínimo: (\d+)\.?$", criterio.strip())
        if not m:
            falhar(onde, f"critério fora do padrão 'grupos [...]; mínimo: N': {criterio!r}")
        grupos_txt = re.sub(r"\]\s+e\s+\[", "], [", m.group(1).strip())
        try:
            grupos = json.loads("[" + grupos_txt.replace("“", '"').replace("”", '"') + "]")
        except json.JSONDecodeError as e:
            falhar(onde, f"grupos de palavras-chave ilegíveis ({e}): {grupos_txt!r}")
        minimo = int(m.group(2))
        if not (1 <= minimo <= len(grupos)):
            falhar(onde, f"mínimo {minimo} incompatível com {len(grupos)} grupos")
        modelo = "\n".join(b["texto"] for b in campos["Resposta esperada"])
        gabarito = {"palavrasChave": grupos, "minimo": minimo, "respostaModelo": modelo}

    enunciado = texto_simples(texto_enunciado)
    if not enunciado:
        falhar(onde, "enunciado vazio")

    numero = int(onde.rsplit("Q", 1)[1])
    return {
        "codigo": onde, "titulo": q["titulo"], "ordenacao": posicao,
        "tipo": TIPOS[tipo_doc], "formato": formato, "pontos": xp,
        "habilidade": habilidade, "enunciado": enunciado,
        "codigo_apoio": juntar_codigo(codigo) if codigo else None,
        "alternativas": alternativas, "gabarito": gabarito,
        "explicacao": explicacao, "dica": dica,
        "_numero": numero, "_boss": tipo_doc.startswith("Boss"),
    }


# ---------------------------------------------------------------------------
# 6) Montagem + CONFERÊNCIAS
# ---------------------------------------------------------------------------
def montar(fases_brutas):
    fases = []
    codigos = set()
    for f in fases_brutas:
        onde_f = f"Fase {f['ordem']}"
        ementa_final = next((b["texto"].split(":", 1)[1].strip() for b in f["ementa"]
                             if b["texto"].startswith("Finalidade pedagógica:")), None)
        if not ementa_final:
            falhar(onde_f, "ementa sem 'Finalidade pedagógica:'")
        descricao = ementa_final[0].upper() + ementa_final[1:]
        if len(descricao) > 255:
            falhar(onde_f, f"Finalidade pedagógica com {len(descricao)} caracteres (modulo.descricao aceita 255)")
        rodadas = []
        for r in f["rodadas"]:
            onde_r = f"{onde_f} Rodada {r['ordem']}"
            conteudo, ordem_declarada = montar_teoria(r, onde_r)
            questoes = []
            for i, q in enumerate(r["questoes"]):
                try:
                    questoes.append(montar_questao(q, f["ordem"], r["ordem"], i + 1))
                except ErroSeed as e:  # junta TODOS os erros antes de parar, para corrigir de uma vez
                    ERROS.append(str(e))
            if ERROS:
                continue
            if [q["codigo"] for q in questoes] != ordem_declarada:
                falhar(onde_r, f"ordem das questões {[q['codigo'] for q in questoes]} "
                               f"diferente da declarada {ordem_declarada}")
            rodadas.append({"ordem": r["ordem"], "titulo": r["titulo"], "conteudo": conteudo, "questoes": questoes})
        fases.append({"ordem": f["ordem"], "titulo": f["titulo"], "descricao": descricao, "rodadas": rodadas})

    if ERROS:
        raise ErroSeed("\n".join(ERROS))
    # ---- conferências globais ----
    if [f["ordem"] for f in fases] != list(range(1, 11)):
        falhar("documento", f"fases encontradas: {[f['ordem'] for f in fases]} (esperado 1..10)")
    total = 0
    for f in fases:
        onde_f = f"Fase {f['ordem']}"
        if len(f["rodadas"]) != 2 or [r["ordem"] for r in f["rodadas"]] != [1, 2]:
            falhar(onde_f, "a fase precisa de exatamente as rodadas 1 e 2")
        xp_fase = 0
        for r in f["rodadas"]:
            for q in r["questoes"]:
                onde = q["codigo"]
                if q["codigo"] in codigos:
                    falhar(onde, "código de questão repetido")
                codigos.add(q["codigo"])
                prefixo = f"F{f['ordem']}." + (f"R{r['ordem']}." if f["ordem"] == 9 else "")
                if not q["codigo"].startswith(prefixo):
                    falhar(onde, f"questão na fase/rodada errada (esperado prefixo {prefixo})")
                tabela = XP_POR_NUMERO_F10 if f["ordem"] == 10 else XP_POR_NUMERO
                if q["pontos"] != tabela[q["_numero"]]:
                    falhar(onde, f"XP {q['pontos']} diferente da regra ({tabela[q['_numero']]})")
                if len(q["habilidade"]) > 255:
                    falhar(onde, f"habilidade com {len(q['habilidade'])} caracteres (máx. 255)")
                if len(q["titulo"]) > 150 or len(q["codigo"]) > 20:
                    falhar(onde, "título > 150 ou código > 20 caracteres")
                for nome in NOMES_PROIBIDOS:
                    if nome in q["enunciado"] + " " + q["explicacao"]:
                        falhar(onde, f"personagem '{nome.strip()}' (regra: só o Gattuccino)")
                xp_fase += q["pontos"]
            bosses = [q for q in r["questoes"] if q["_boss"]]
            # Regra: o Boss fica na Rodada 2. Exceção documentada: na Fase 9 CADA rodada tem o seu Boss.
            precisa_boss = r["ordem"] == 2 or f["ordem"] == 9
            if not precisa_boss and bosses:
                falhar(onde_f, f"Boss na Rodada {r['ordem']}")
            if precisa_boss and (len(bosses) != 1 or r["questoes"][-1] is not bosses[0]):
                falhar(onde_f, f"a Rodada {r['ordem']} precisa terminar com exatamente 1 Boss")
        if xp_fase != XP_ESPERADO_FASE[f["ordem"]]:
            falhar(onde_f, f"XP da fase {xp_fase} (esperado {XP_ESPERADO_FASE[f['ordem']]})")
        total += xp_fase
    if len(codigos) != 110 or total != 2400:
        falhar("documento", f"{len(codigos)} questões / {total} XP (esperado 110 / 2400)")
    nao_usados = set(AJUSTES) - codigos
    if nao_usados:
        falhar("ajustes_seed.py", f"ajustes para questões que não existem: {sorted(nao_usados)}")
    return fases


# ---------------------------------------------------------------------------
# 7) SQL
# ---------------------------------------------------------------------------
def sql(valor):
    """Literal SQL seguro: NULL, número ou texto entre aspas simples com escapes do MySQL."""
    if valor is None:
        return "NULL"
    if isinstance(valor, int):
        return str(valor)
    if not isinstance(valor, str):
        valor = json.dumps(valor, ensure_ascii=False)
    return "'" + valor.replace("\\", "\\\\").replace("'", "''") + "'"


def gerar_sql(fases):
    linhas = [
        "-- =====================================================================",
        "-- LogiCup — Seed do conteúdo pedagógico: Fases 1 a 10 (110 questões, 2.400 XP)",
        "-- Fonte: BancoDeQuestoesLogiCup_v3_0.docx (Banco de Questões Mestre, versão 3.0)",
        "-- Arquivo GERADO por gerar_seed.py — NÃO edite à mão: corrija o documento (ou",
        "-- ajustes_seed.py) e rode o gerador de novo.",
        "--",
        "-- QUANDO RODAR: num banco RECÉM-CRIADO pelo 01_schema.sql (tabelas de conteúdo vazias).",
        "-- Tudo ou nada: se qualquer comando falhar, o ROLLBACK desfaz o que foi feito.",
        "-- Ao final, rode 03_conferencia_seed.sql e confira os valores esperados.",
        "-- =====================================================================",
        "USE Logicup;",
        "SET NAMES utf8mb4;",
        "",
        "-- Trava de segurança: só continua se NÃO houver conteúdo (evita duplicar fases).",
        "SELECT IF((SELECT COUNT(*) FROM modulo) = 0, 'OK: banco vazio, pode seguir',",
        "          'PARE: já existe conteúdo — este seed é só para banco recém-criado') AS conferencia_inicial;",
        "",
        "START TRANSACTION;",
    ]
    for f in fases:
        linhas += ["", f"-- ---------------- FASE {f['ordem']} — {f['titulo']} ----------------",
                   "INSERT INTO modulo (titulo, descricao, ordenacao) VALUES "
                   f"({sql(f['titulo'])}, {sql(f['descricao'])}, {f['ordem']});",
                   "SET @id_modulo = LAST_INSERT_ID();"]
        for r in f["rodadas"]:
            linhas += ["", f"-- Rodada {r['ordem']}: {r['titulo']}",
                       "INSERT INTO rodada (id_modulo, titulo, ordenacao, conteudo_teorico) VALUES "
                       f"(@id_modulo, {sql(r['titulo'])}, {r['ordem']}, {sql(r['conteudo'])});",
                       "SET @id_rodada = LAST_INSERT_ID();"]
            for q in r["questoes"]:
                linhas.append(f"-- {q['codigo']}: {q['titulo']} ({q['tipo']} / {q['formato']}, {q['pontos']} XP)")
                linhas.append(
                    "INSERT INTO exercicio (id_rodada, codigo_questao, titulo, ordenacao, tipo_exercicio, "
                    "formato_resposta, pontos, habilidade, enunciado, codigo_apoio, alternativas_json, "
                    "gabarito_json, explicacao) VALUES (@id_rodada, "
                    + ", ".join(sql(v) for v in [q["codigo"], q["titulo"], q["ordenacao"], q["tipo"],
                                                 q["formato"], q["pontos"], q["habilidade"], q["enunciado"],
                                                 q["codigo_apoio"], q["alternativas"], q["gabarito"],
                                                 q["explicacao"]]) + ");")
                if q["dica"]:
                    linhas.append(f"INSERT INTO dica (id_exercicio, texto_dica) VALUES (LAST_INSERT_ID(), {sql(q['dica'])});")
    linhas += ["", "COMMIT;", ""]
    return "\n".join(linhas)


def converter_questoes_de_codigo(fases):
    """Sprint 9 (ADR-001): o documento descreve as questões "Escrever código" como TEXTO_LIVRE (palavras-chave).
    As que têm programa de teste VALIDADO (codigo/questoes_codigo.json) passam a CODIGO, com as mesmas regras
    da migração 07b (aplicar_codigo.converter). Para quando faltar alguma: nada de seed pela metade."""
    dados = aplicar_codigo.carregar()
    encontradas = set()
    for f in fases:
        for r in f["rodadas"]:
            for q in r["questoes"]:
                if q["codigo"] in dados:
                    try:
                        aplicar_codigo.converter(q, dados[q["codigo"]])
                    except ValueError as e:
                        falhar(q["codigo"], str(e))
                    encontradas.add(q["codigo"])
    faltando = set(dados) - encontradas
    if faltando:
        falhar("questoes_codigo.json", f"questões que não existem no documento: {sorted(faltando)}")


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    try:
        fases = montar(recortar(ler_blocos(sys.argv[1])))
        converter_questoes_de_codigo(fases)
    except ErroSeed as e:
        print("ERRO — nada foi gravado.\n" + str(e))
        sys.exit(1)
    (PASTA_SQL / "02_seed_conteudo.sql").write_text(gerar_sql(fases), encoding="utf-8")
    revisao = [{k: v for k, v in f.items()} for f in fases]
    (PASTA / "seed_revisao.json").write_text(json.dumps(revisao, ensure_ascii=False, indent=2), encoding="utf-8")
    n = sum(len(r["questoes"]) for f in fases for r in f["rodadas"])
    dicas = sum(1 for f in fases for r in f["rodadas"] for q in r["questoes"] if q["dica"])
    print(f"OK: {len(fases)} fases, {sum(len(f['rodadas']) for f in fases)} rodadas, {n} questões, {dicas} dicas.")
    print(f"Gerados: {PASTA_SQL / '02_seed_conteudo.sql'} e {PASTA / 'seed_revisao.json'}")


if __name__ == "__main__":
    main()
