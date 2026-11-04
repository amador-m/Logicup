"""
Sprint 9 (ADR-001): o que muda numa questão quando ela passa a ser corrigida por EXECUÇÃO (formato CODIGO).

Usado por DOIS lugares, para eles nunca ficarem diferentes:
  - gerar_seed.py           -> banco NOVO (instalação do zero / produção)
  - gerar_migracao_07b.py   -> banco que JÁ EXISTE (o seu, com alunos e progresso)

Regra:
  formato_resposta  TEXTO_LIVRE -> CODIGO
  gabarito_json     MANTÉM palavrasChave, minimo e respostaModelo (reserva + resposta exibida)
                    e GANHA modo, comparacao, programaTeste, programaExemplo e casos (SECRETO)
  alternativas_json passa a ter o que a TELA precisa (PÚBLICO): modo, código inicial do editor,
                    exemplos do botão Executar e a saída esperada desses exemplos
"""
import json
from pathlib import Path

ARQUIVO = Path(__file__).resolve().parent / "questoes_codigo.json"
ARQUIVO_BLOCOS = Path(__file__).resolve().parent / "questoes_blocos.json"

# campos que vão para o gabarito (secreto) e para as alternativas (público)
CAMPOS_GABARITO = ["modo", "comparacao", "programaTeste", "programaExemplo", "casos"]


def carregar(arquivo=ARQUIVO):
    if not arquivo.exists():
        raise SystemExit(f"Falta {arquivo}. Rode antes: python3 codigo/gerar_questoes_codigo.py")
    return json.loads(arquivo.read_text(encoding="utf-8"))


def carregar_blocos():
    return carregar(ARQUIVO_BLOCOS)


def parte_secreta(dados):
    return {campo: dados[campo] for campo in CAMPOS_GABARITO}


def parte_publica(dados):
    return {"modo": dados["modo"], "codigoInicial": dados["codigoInicial"],
            "exemplos": dados["exemplos"], "saidaDosExemplos": dados["saidaDosExemplos"]}


def converter(questao, dados):
    """Converte o dicionário de uma questão do seed (formato TEXTO_LIVRE) para CODIGO."""
    if questao["formato"] != "TEXTO_LIVRE":
        raise ValueError(f"{questao['codigo']}: esperado TEXTO_LIVRE, encontrado {questao['formato']}")
    gabarito = dict(questao["gabarito"])
    for campo in ("palavrasChave", "minimo", "respostaModelo"):
        if campo not in gabarito:
            raise ValueError(f"{questao['codigo']}: o gabarito não tem {campo} (necessário para a reserva)")
    gabarito.update(parte_secreta(dados))
    questao.update(formato="CODIGO", gabarito=gabarito, alternativas=parte_publica(dados))
    return questao


# ---------------------------------------------------------------------------------------------------------
# Sprint 9 (ADR-001, etapa 1 do Blockly): questões de BLOCOS.
#   Usado por gerar_seed.py (banco novo) e gerar_migracao_08.py (banco que já existe), pelo mesmo motivo.
#
#   formato_resposta  ORDENACAO -> BLOCOS
#   gabarito_json     é TROCADO: sai {"ordem": [...]} (a ordem das linhas) e entra o gabarito de código
#                     (palavrasChave, minimo, respostaModelo + modo, comparacao, programaTeste, programaExemplo, casos)
#   alternativas_json é TROCADO: saem as linhas para ordenar e entram os blocos iniciais (PÚBLICO)
#   enunciado, explicação, dica, XP e progresso dos alunos: NÃO mudam
# ---------------------------------------------------------------------------------------------------------
CAMPOS_RESERVA = ["palavrasChave", "minimo", "respostaModelo"]


def gabarito_blocos(dados):
    return {**{campo: dados[campo] for campo in CAMPOS_RESERVA}, **parte_secreta(dados)}


def alternativas_blocos(dados):
    alternativas = {"modo": dados["modo"], "blocosIniciais": dados["blocosIniciais"],
                    "exemplos": dados["exemplos"], "saidaDosExemplos": None}
    if dados.get("sobram"):
        alternativas["sobram"] = True  # 03/11: há blocos que não devem ser usados (F2.Q4)
    return alternativas


# 03/11: textos que a questão de blocos pode substituir (o documento dizia "organize as linhas")
TEXTOS = ("enunciado", "explicacao", "habilidade")
TIPOS_QUE_VIRAM_BLOCOS = ("BLOCOS", "ORDEM_CORRETA")


def converter_blocos(questao, dados):
    """Converte o dicionário de uma questão do seed (formato ORDENACAO) para BLOCOS.
    03/11: além da F5.Q8 (tipo BLOCOS no documento), questões ORDEM_CORRETA também viram blocos; o tipo
    passa a BLOCOS (é ele que aparece na etiqueta da questão: "Blocos")."""
    if questao["formato"] != "ORDENACAO":
        raise ValueError(f"{questao['codigo']}: esperado ORDENACAO, encontrado {questao['formato']}")
    if questao["tipo"] not in TIPOS_QUE_VIRAM_BLOCOS:
        raise ValueError(f"{questao['codigo']}: tipo {questao['tipo']} não vira blocos")
    questao.update(tipo="BLOCOS", formato="BLOCOS", gabarito=gabarito_blocos(dados),
                   alternativas=alternativas_blocos(dados))
    questao.update({campo: dados[campo] for campo in TEXTOS if campo in dados})
    return questao
