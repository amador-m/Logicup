"""
Sprint 9 (ADR-001): gera e VALIDA a correção por execução das 9 questões "Escrever código"
e das questões de BLOCOS (questoes_blocos.py; elas são corrigidas do mesmo jeito).

O que faz, para cada questão de questoes_codigo.py:
  1. monta o programa de teste com a solução de REFERÊNCIA e executa com Java (no modo do Piston:
     "java Main.java", restrito ao Java 15 com --source 15);
  2. guarda a saída de cada caso como a SAÍDA ESPERADA (assim o gabarito nunca contradiz a solução);
  3. VALIDA: todas as soluções "certas" precisam passar e todos os erros "errados" precisam reprovar.
     Se alguma validação falhar, o script PARA e nada é gerado (não vai gabarito errado para o banco).
  4. escreve questoes_codigo.json e questoes_blocos.json (lidos pelas migrações e pelo gerador do seed).

A montagem do programa segue exatamente as mesmas regras da classe Java MontadorPrograma do backend.

Uso: python3 gerar_questoes_codigo.py   (precisa de um JDK no PATH; qualquer versão >= 15)
"""
import json
import os
import re
import subprocess
import sys
import tempfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from questoes_codigo import QUESTOES, MARCA  # noqa: E402
from questoes_blocos import QUESTOES_BLOCOS  # noqa: E402  Sprint 9: questões de BLOCOS (mesma validação)

PONTO = "//@CODIGO_DO_ALUNO"
IMPORT = re.compile(r"^\s*import\s+(static\s+)?[\w.]+(\.\*)?\s*;\s*$")
PACKAGE = re.compile(r"^\s*package\s+[\w.]+\s*;\s*$")
NUMERO = re.compile(r"-?\d+")


def montar(template, codigo):
    """Mesmas regras do MontadorPrograma.java: imports do aluno sobem para o topo (a linha vira vazia,
    para não mudar a numeração), 'package' é removido, e o código entra no lugar de //@CODIGO_DO_ALUNO."""
    codigo = codigo.replace("\r\n", "\n")
    imports, linhas = [], []
    for linha in codigo.split("\n"):
        if IMPORT.match(linha):
            imports.append(linha.strip())
            linhas.append("")
        elif PACKAGE.match(linha):
            linhas.append("")
        else:
            linhas.append(linha)
    assert template.count(PONTO) == 1, "o programa precisa ter exatamente um " + PONTO
    corpo = template.replace(PONTO, "\n".join(linhas))
    return "\n".join(imports) + ("\n" if imports else "") + corpo


def executar(programa):
    with tempfile.TemporaryDirectory() as pasta:
        arquivo = os.path.join(pasta, "Main.java")
        with open(arquivo, "w", encoding="utf-8") as f:
            f.write(programa)
        env = {k: v for k, v in os.environ.items() if k != "JAVA_TOOL_OPTIONS"}
        try:
            r = subprocess.run(["java", "--source", "15", "-Dfile.encoding=UTF-8", arquivo],
                               capture_output=True, timeout=20, env=env, cwd=pasta)
            return r.returncode, r.stdout.decode("utf-8"), r.stderr.decode("utf-8")
        except subprocess.TimeoutExpired:
            return None, "", "TEMPO ESGOTADO"


def normalizar(texto):
    linhas = [l.rstrip() for l in texto.replace("\r\n", "\n").split("\n")]
    while linhas and linhas[0] == "":
        linhas.pop(0)
    while linhas and linhas[-1] == "":
        linhas.pop()
    return "\n".join(linhas)


def iguais(comparacao, esperado, obtido):
    """Mesmas regras do ComparadorSaida.java."""
    if comparacao == "EXATA":
        return normalizar(esperado) == normalizar(obtido)
    if comparacao == "NUMEROS":
        return NUMERO.findall(esperado) == NUMERO.findall(obtido)
    if comparacao == "ULTIMO_NUMERO":
        e, o = NUMERO.findall(esperado), NUMERO.findall(obtido)
        return bool(e) and bool(o) and e[-1] == o[-1]
    raise ValueError(comparacao)


def segmentos(saida):
    return saida.split(MARCA)[1:]


def avaliar(q, saidas_esperadas, codigo):
    """True se o código passa em TODOS os casos (mesma lógica do CorretorCodigo, sem a parte do Piston)."""
    codigo_saida, saida, _ = executar(montar(q["teste"], codigo))
    partes = segmentos(saida)
    if len(partes) < len(saidas_esperadas):
        return False, f"parou no caso {len(partes)} (código de saída {codigo_saida})"
    for i, esperada in enumerate(saidas_esperadas):
        if not iguais(q["comparacao"], esperada, partes[i]):
            return False, f"caso {i + 1} ({q['casos'][i]}): esperado {normalizar(esperada)!r}, obtido {normalizar(partes[i])!r}"
    if codigo_saida != 0:
        return False, f"terminou com código {codigo_saida}"
    return True, "ok"


def validar(questoes, problemas):
    """Gera as saídas esperadas e valida cada questão. Devolve {codigo: dados} (os problemas vão para a lista)."""
    resultado = {}
    for codigo_questao, q in questoes.items():
        assert len(q["casos"]) > 0
        # 1-2) saídas esperadas a partir da referência
        rc, saida, erros = executar(montar(q["teste"], q["referencia"]))
        if rc != 0:
            problemas.append(f"{codigo_questao}: a REFERÊNCIA não roda: {erros[:500]}")
            continue
        esperadas = [normalizar(p) for p in segmentos(saida)]
        if len(esperadas) != len(q["casos"]):
            problemas.append(f"{codigo_questao}: {len(esperadas)} marcas no programa, {len(q['casos'])} casos descritos")
            continue
        rc_ex, saida_exemplo, erros_ex = executar(montar(q["exemplo"], q["referencia"]))
        if rc_ex != 0:
            problemas.append(f"{codigo_questao}: o programa de EXEMPLO não roda: {erros_ex[:500]}")
            continue

        # 3) validação
        print(f"\n== {codigo_questao} ({q['modo']}, {q['comparacao']}, {len(esperadas)} casos)")
        for i, solucao in enumerate([q["referencia"]] + q["certas"]):
            ok, motivo = avaliar(q, esperadas, solucao)
            print(f"   certa #{i}: {'PASSA' if ok else 'REPROVA (ERRO!) ' + motivo}")
            if not ok:
                problemas.append(f"{codigo_questao}: solução certa #{i} reprovou: {motivo}")
        for nome, solucao in q["erradas"].items():
            ok, motivo = avaliar(q, esperadas, solucao)
            print(f"   errada '{nome}': {'PASSA (ERRO!)' if ok else 'reprova -> ' + motivo[:110]}")
            if ok:
                problemas.append(f"{codigo_questao}: o erro '{nome}' PASSOU")

        resultado[codigo_questao] = {
            "modo": q["modo"],
            "comparacao": q["comparacao"],
            "codigoInicial": q["codigo_inicial"],
            "programaTeste": q["teste"],
            "programaExemplo": q["exemplo"],
            "casos": [{"descricao": d, "saida": s} for d, s in zip(q["casos"], esperadas)],
            "exemplos": q["exemplos"],
            "saidaDosExemplos": normalizar(saida_exemplo),
        }
    return resultado


def main():
    problemas = []
    resultado = validar(QUESTOES, problemas)
    # Sprint 9 (Blockly): as questões de blocos são validadas do mesmo jeito (a referência é o Java gerado pelos blocos)
    blocos = validar(QUESTOES_BLOCOS, problemas)
    for codigo_questao, dados in blocos.items():
        q = QUESTOES_BLOCOS[codigo_questao]
        dados.pop("codigoInicial")
        dados.update(blocosIniciais=q["blocos_iniciais"], palavrasChave=q["palavras_chave"],
                     minimo=q["minimo"], respostaModelo=q["resposta_modelo"], sobram=q.get("sobram", False))
        # 03/11: textos que substituem os do documento ("organize as linhas" -> "encaixe os blocos")
        for campo in ("enunciado", "explicacao", "habilidade"):
            if campo in q:
                dados[campo] = q[campo]

    if problemas:
        print("\n*** VALIDAÇÃO FALHOU — nada foi gerado ***")
        for p in problemas:
            print(" -", p)
        sys.exit(1)

    pasta = os.path.dirname(os.path.abspath(__file__))
    destino = os.path.join(pasta, "questoes_codigo.json")
    with open(destino, "w", encoding="utf-8") as f:
        json.dump(resultado, f, ensure_ascii=False, indent=2)
    destino_blocos = os.path.join(pasta, "questoes_blocos.json")
    with open(destino_blocos, "w", encoding="utf-8") as f:
        json.dump(blocos, f, ensure_ascii=False, indent=2)

    # Arquivo para o teste JUnit com o Piston REAL (QuestoesCodigoRealTest): gabarito + a solução de
    # referência (precisa passar) + o primeiro erro típico (precisa reprovar). Vai para src/test/resources.
    fixture = {}
    todas = {**QUESTOES, **QUESTOES_BLOCOS}
    for codigo_questao, dados in {**resultado, **blocos}.items():
        q = todas[codigo_questao]
        nome_erro, codigo_erro = next(iter(q["erradas"].items()))
        fixture[codigo_questao] = {"gabarito": dados, "referencia": q["referencia"],
                                   "erro": {"nome": nome_erro, "codigo": codigo_erro}}
    destino_teste = os.path.join(pasta, "questoes-codigo-teste.json")
    with open(destino_teste, "w", encoding="utf-8") as f:
        json.dump(fixture, f, ensure_ascii=False, indent=2)
    print(f"\nOK: {len(resultado)} questões de código e {len(blocos)} de blocos validadas -> "
          f"{destino}, {destino_blocos} e {destino_teste}")


if __name__ == "__main__":
    main()
