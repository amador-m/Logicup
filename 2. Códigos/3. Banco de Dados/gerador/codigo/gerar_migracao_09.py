"""
03/11: gera 09_migracao_mais_blocos.sql e 09_reverter_mais_blocos.sql.

Mais 5 questões de ORDENAÇÃO de linhas passam a ser montadas com BLOCOS: F1.Q4, F2.Q4, F4.Q5, F9.R2.Q8 e F10.Q5.
Lê questoes_blocos.json (gerado e VALIDADO pelo gerar_questoes_codigo.py) e usa as MESMAS regras do gerador
do seed (aplicar_codigo.converter_blocos), então o banco migrado fica igual a um banco novo.
A F5.Q8 (migração 08) não é tocada.
Uso: python3 codigo/gerar_migracao_09.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from aplicar_codigo import TEXTOS, alternativas_blocos, carregar_blocos, gabarito_blocos  # noqa: E402

PASTA_SQL = Path(__file__).resolve().parent.parent.parent  # "3. Banco de Dados/"
BACKUP = "exercicio_backup_0311_blocos"
QUESTOES = ["F1.Q4", "F2.Q4", "F4.Q5", "F9.R2.Q8", "F10.Q5"]


def sql(texto):
    """Literal SQL seguro (as mesmas regras do gerar_seed.py)."""
    return "'" + texto.replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    todas = carregar_blocos()
    questoes = {codigo: todas[codigo] for codigo in QUESTOES}
    codigos = ", ".join(f"'{c}'" for c in questoes)
    n = len(questoes)

    linhas = [
        "-- =====================================================================",
        "-- LogiCup — Migração 09 (03/11): mais 5 questões passam de ORDENAÇÃO de linhas para BLOCOS:",
        "--   F1.Q4 (passos do café), F2.Q4 (programa completo, mais difícil), F4.Q5 (if/else),",
        "--   F9.R2.Q8 (subclasse) e F10.Q5 (método que percorre a lista).",
        "-- GERADO por gerador/codigo/gerar_migracao_09.py.",
        "--",
        "-- PRÉ-REQUISITOS: a migração 08 (F5.Q8 em BLOCOS) e o frontend com os blocos novos",
        "--   (algoritmo/passo e blocos de linha). Sem o frontend novo, essas questões não abrem direito.",
        "--",
        "-- O QUE MUDA (só nessas 5):",
        "--   tipo_exercicio    ORDEM_CORRETA -> BLOCOS (a etiqueta da questão passa a ser \"Blocos\")",
        "--   formato_resposta  ORDENACAO -> BLOCOS",
        "--   gabarito_json     sai a ordem das linhas; entra o programa de teste, os casos e a reserva",
        "--   alternativas_json saem as linhas; entram os blocos iniciais",
        "--   enunciado         \"organize as linhas\" -> \"encaixe os blocos\" (F2.Q4: enunciado novo, mais difícil)",
        "--   explicacao        só F2.Q4 e F4.Q5;  habilidade: só F2.Q4",
        "-- O QUE NÃO MUDA: título, XP (a F2.Q4 continua com 15) e o PROGRESSO dos alunos.",
        "--   A F5.Q4 continua como ordenação (decisão de 03/11), e as outras questões não são tocadas.",
        "--",
        "-- SEGURANÇA DOS DADOS:",
        f"--   PASSO 1 cria a tabela {BACKUP} com as linhas ANTES da mudança.",
        "--   Se a tabela já existir, o script PARA (não sobrescreve um backup antigo).",
        "--   PASSO 2 faz os UPDATEs numa transação e mostra a conferência. Só fica gravado com COMMIT.",
        "--   Para voltar atrás depois do COMMIT: 09_reverter_mais_blocos.sql.",
        "--",
        "-- Quem montou o banco do zero com o 02_seed_conteudo.sql NOVO já tem tudo isso: esta migração não é",
        "-- necessária (o backup fica com 0 linhas e nada é alterado).",
        "-- =====================================================================",
        "USE Logicup;",
        "-- OBRIGATÓRIO: sem isto, alguns clientes leem o arquivo como latin1 e estragam os acentos",
        "SET NAMES utf8mb4;",
        "",
        "-- PASSO 1: backup",
        f"CREATE TABLE {BACKUP} AS",
        "  SELECT id_exercicio, codigo_questao, tipo_exercicio, formato_resposta, enunciado, explicacao, habilidade,",
        "         alternativas_json, gabarito_json, NOW() AS copiado_em",
        f"    FROM exercicio WHERE codigo_questao IN ({codigos}) AND formato_resposta = 'ORDENACAO';",
        f"SELECT COUNT(*) AS linhas_no_backup, '{n} esperadas' AS esperado FROM {BACKUP};",
        "",
        "-- PASSO 2: a mudança",
        "START TRANSACTION;",
        "",
    ]
    for codigo, dados in questoes.items():
        textos = "".join(f"       {campo} = {sql(dados[campo])},\n" for campo in TEXTOS if campo in dados)
        linhas += [
            f"-- {codigo}: {dados['modo']} / {dados['comparacao']} / {len(dados['casos'])} caso(s) de teste",
            "UPDATE exercicio",
            "   SET tipo_exercicio = 'BLOCOS',",
            "       formato_resposta = 'BLOCOS',",
            textos.rstrip("\n"),
            f"       gabarito_json = {sql(json.dumps(gabarito_blocos(dados), ensure_ascii=False))},",
            f"       alternativas_json = {sql(json.dumps(alternativas_blocos(dados), ensure_ascii=False))}",
            f" WHERE codigo_questao = '{codigo}' AND formato_resposta = 'ORDENACAO' AND tipo_exercicio = 'ORDEM_CORRETA';",
            "",
        ]
    linhas += [
        "-- CONFERÊNCIA (tudo precisa dar OK antes do COMMIT)",
        "SELECT codigo_questao, tipo_exercicio, formato_resposta, pontos,",
        "       JSON_LENGTH(alternativas_json, '$.blocosIniciais.blocks.blocks') AS blocos_iniciais,",
        "       JSON_LENGTH(gabarito_json, '$.casos') AS casos,",
        "       IF(LOCATE('//@CODIGO_DO_ALUNO', JSON_UNQUOTE(JSON_EXTRACT(gabarito_json, '$.programaTeste'))) > 0,",
        "          'OK', 'SEM ENCAIXE') AS programa",
        f"  FROM exercicio WHERE codigo_questao IN ({codigos}) ORDER BY id_exercicio;",
        "",
        "SELECT CASE WHEN",
        "   (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'BLOCOS' AND tipo_exercicio = 'BLOCOS') = 6",
        "   AND (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ORDENACAO') = 1",
        "   AND (SELECT COUNT(*) FROM exercicio) = 110",
        "   AND (SELECT pontos FROM exercicio WHERE codigo_questao = 'F2.Q4') = 15",
        "   AND (SELECT enunciado FROM exercicio WHERE codigo_questao = 'F4.Q5') LIKE '%Subiu de nível!%'",
        "   THEN 'OK: pode executar COMMIT;' ELSE 'ALGO ERRADO: execute ROLLBACK; e me mande o resultado' END AS resultado;",
        "-- (a última condição confere os acentos: se aparecer \"nÃ­vel\", o SET NAMES não funcionou)",
        "",
        "-- Se o resultado for OK:      COMMIT;",
        f"-- Se não:                     ROLLBACK;   (o backup do PASSO 1 continua lá; apague com DROP TABLE {BACKUP};)",
        "",
    ]
    (PASTA_SQL / "09_migracao_mais_blocos.sql").write_text("\n".join(linhas), encoding="utf-8")

    reverter = [
        "-- =====================================================================",
        "-- LogiCup — DESFAZ a migração 09: as 5 questões voltam EXATAMENTE como estavam (ORDENAÇÃO de linhas),",
        f"-- a partir do backup {BACKUP}. O progresso dos alunos não é alterado. A F5.Q8 não é tocada.",
        "-- =====================================================================",
        "USE Logicup;",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        "UPDATE exercicio e",
        f"  JOIN {BACKUP} b ON b.id_exercicio = e.id_exercicio",
        "   SET e.tipo_exercicio = b.tipo_exercicio,",
        "       e.formato_resposta = b.formato_resposta,",
        "       e.enunciado = b.enunciado,",
        "       e.explicacao = b.explicacao,",
        "       e.habilidade = b.habilidade,",
        "       e.alternativas_json = b.alternativas_json,",
        "       e.gabarito_json = b.gabarito_json;",
        "",
        f"-- CONFERÊNCIA: as linhas iguais ao backup (esperado: {n}) e só a F5.Q8 em BLOCOS",
        "SELECT SUM(e.tipo_exercicio = b.tipo_exercicio AND e.formato_resposta = b.formato_resposta",
        "           AND e.enunciado = b.enunciado AND e.explicacao = b.explicacao",
        "           AND (e.habilidade <=> b.habilidade) AND e.gabarito_json = b.gabarito_json",
        "           AND (e.alternativas_json <=> b.alternativas_json)) AS iguais_ao_backup,",
        "       (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'BLOCOS') AS em_blocos",
        f"  FROM exercicio e JOIN {BACKUP} b ON b.id_exercicio = e.id_exercicio;",
        "",
        f"-- Se iguais_ao_backup = {n} e em_blocos = 1:  COMMIT;",
        "-- Se não:                                   ROLLBACK;",
        "",
    ]
    (PASTA_SQL / "09_reverter_mais_blocos.sql").write_text("\n".join(reverter), encoding="utf-8")
    print(f"OK: 09_migracao_mais_blocos.sql e 09_reverter_mais_blocos.sql ({n} questões)")


if __name__ == "__main__":
    main()
