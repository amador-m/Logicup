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

# campos que vão para o gabarito (secreto) e para as alternativas (público)
CAMPOS_GABARITO = ["modo", "comparacao", "programaTeste", "programaExemplo", "casos"]


def carregar():
    if not ARQUIVO.exists():
        raise SystemExit(f"Falta {ARQUIVO}. Rode antes: python3 codigo/gerar_questoes_codigo.py")
    return json.loads(ARQUIVO.read_text(encoding="utf-8"))


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
