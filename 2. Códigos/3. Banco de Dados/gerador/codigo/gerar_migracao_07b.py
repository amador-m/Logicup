"""
Sprint 9 (ADR-001): gera 07b_migracao_questoes_codigo.sql e 07b_reverter_questoes_codigo.sql.

Lê questoes_codigo.json (gerado e VALIDADO pelo gerar_questoes_codigo.py) e escreve os UPDATEs das 9 questões.
O gabarito do banco é COMPLETADO com JSON_MERGE_PATCH (as palavras-chave e a resposta modelo que já
estão no banco ficam como estão). Uso: python3 codigo/gerar_migracao_07b.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from aplicar_codigo import carregar, parte_publica, parte_secreta  # noqa: E402

# gerador/codigo/ -> gerador/ -> "3. Banco de Dados/" (onde ficam os .sql, igual ao gerar_seed.py)
PASTA_SQL = Path(__file__).resolve().parent.parent.parent


def sql(texto):
    """Literal SQL seguro (as mesmas regras do gerar_seed.py)."""
    return "'" + texto.replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    questoes = carregar()
    codigos = ", ".join(f"'{c}'" for c in questoes)
    n = len(questoes)

    linhas = [
        "-- =====================================================================",
        "-- LogiCup — Migração 07b (Sprint 9, ADR-001): as 9 questões \"Escrever código\" passam a ser",
        "-- corrigidas EXECUTANDO o código (formato CODIGO). GERADO por gerador/codigo/gerar_migracao_07b.py.",
        "--",
        "-- PRÉ-REQUISITOS: (1) migração 07a aplicada; (2) backend da Sprint 9 parte 2; (3) frontend com o editor",
        "--   de código (sem ele, essas 9 questões aparecem como \"formato não suportado\").",
        "--",
        "-- O QUE MUDA (só nas 9 questões abaixo):",
        "--   formato_resposta  TEXTO_LIVRE -> CODIGO",
        "--   gabarito_json     MANTÉM palavrasChave/minimo/respostaModelo e GANHA o programa de teste e os casos",
        "--   alternativas_json passa a ter o código inicial do editor e os exemplos públicos",
        "-- O QUE NÃO MUDA: o progresso dos alunos (quem já concluiu continua concluído, com o mesmo XP),",
        "--   as outras 101 questões e a F5.Q8 (continua ORDENACAO até o Blockly).",
        "--",
        "-- SEGURANÇA DOS DADOS:",
        "--   PASSO 1 cria a tabela exercicio_backup_s9 com as 9 linhas ANTES da mudança.",
        "--   Se a tabela já existir, o script PARA (não sobrescreve um backup antigo).",
        "--   PASSO 2 faz os UPDATEs numa transação e mostra a conferência. Só fica gravado com COMMIT.",
        "--   Para voltar atrás depois do COMMIT: 07b_reverter_questoes_codigo.sql.",
        "-- =====================================================================",
        "USE Logicup;",
        "-- OBRIGATÓRIO: sem isto, alguns clientes leem o arquivo como latin1 e gravam \"café\" como \"cafÃ©\"",
        "SET NAMES utf8mb4;",
        "",
        "-- PASSO 1: backup (CREATE TABLE ... AS SELECT copia estrutura e dados das 9 linhas)",
        "CREATE TABLE exercicio_backup_s9 AS",
        "  SELECT id_exercicio, codigo_questao, formato_resposta, alternativas_json, gabarito_json, NOW() AS copiado_em",
        f"    FROM exercicio WHERE codigo_questao IN ({codigos});",
        f"SELECT COUNT(*) AS linhas_no_backup, '{n} esperadas' AS esperado FROM exercicio_backup_s9;",
        "",
        "-- PASSO 2: a mudança",
        "START TRANSACTION;",
        "",
    ]
    for codigo, dados in questoes.items():
        linhas += [
            f"-- {codigo}: {dados['modo']} / {dados['comparacao']} / {len(dados['casos'])} casos de teste",
            "UPDATE exercicio",
            "   SET formato_resposta = 'CODIGO',",
            f"       gabarito_json = JSON_MERGE_PATCH(gabarito_json, {sql(json.dumps(parte_secreta(dados), ensure_ascii=False))}),",
            f"       alternativas_json = {sql(json.dumps(parte_publica(dados), ensure_ascii=False))}",
            f" WHERE codigo_questao = '{codigo}' AND formato_resposta = 'TEXTO_LIVRE';",
            "",
        ]
    linhas += [
        "-- CONFERÊNCIA (tudo precisa dar OK antes do COMMIT)",
        "SELECT codigo_questao, formato_resposta,",
        "       JSON_LENGTH(gabarito_json, '$.casos') AS casos,",
        "       IF(JSON_EXTRACT(gabarito_json, '$.palavrasChave') IS NOT NULL",
        "          AND JSON_EXTRACT(gabarito_json, '$.respostaModelo') IS NOT NULL, 'OK', 'FALTA RESERVA') AS reserva,",
        "       IF(LOCATE('//@CODIGO_DO_ALUNO', JSON_UNQUOTE(JSON_EXTRACT(gabarito_json, '$.programaTeste'))) > 0,",
        "          'OK', 'SEM ENCAIXE') AS programa",
        f"  FROM exercicio WHERE codigo_questao IN ({codigos}) ORDER BY id_exercicio;",
        "",
        "SELECT CASE WHEN",
        f"   (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'CODIGO') = {n}",
        "   AND (SELECT COUNT(*) FROM exercicio) = 110",
        f"   AND (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'TEXTO_LIVRE') = 4",
        "   THEN 'OK: pode executar COMMIT;' ELSE 'ALGO ERRADO: execute ROLLBACK; e me mande o resultado' END AS resultado;",
        "",
        "-- Se o resultado for OK:      COMMIT;",
        "-- Se não:                     ROLLBACK;   (o backup do PASSO 1 continua lá; apague com DROP TABLE exercicio_backup_s9;)",
        "",
    ]
    (PASTA_SQL / "07b_migracao_questoes_codigo.sql").write_text("\n".join(linhas), encoding="utf-8")

    reverter = [
        "-- =====================================================================",
        "-- LogiCup — DESFAZ a migração 07b: as 9 questões voltam EXATAMENTE como estavam (a partir do backup).",
        "-- Use só se precisar voltar à correção por palavras-chave (ex.: problema com o executor na banca).",
        "-- O progresso dos alunos não é alterado.",
        "-- =====================================================================",
        "USE Logicup;",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        "UPDATE exercicio e",
        "  JOIN exercicio_backup_s9 b ON b.id_exercicio = e.id_exercicio",
        "   SET e.formato_resposta = b.formato_resposta,",
        "       e.alternativas_json = b.alternativas_json,",
        "       e.gabarito_json = b.gabarito_json;",
        "",
        "-- CONFERÊNCIA: as 9 linhas iguais ao backup (esperado: 9 iguais) e nenhuma questão CODIGO",
        "SELECT SUM(e.formato_resposta = b.formato_resposta",
        "           AND e.gabarito_json = b.gabarito_json",
        "           AND (e.alternativas_json <=> b.alternativas_json)) AS iguais_ao_backup,",
        "       (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'CODIGO') AS ainda_codigo",
        "  FROM exercicio e JOIN exercicio_backup_s9 b ON b.id_exercicio = e.id_exercicio;",
        "",
        "-- Se iguais_ao_backup = 9 e ainda_codigo = 0:  COMMIT;",
        "-- Se não:                                      ROLLBACK;",
        "",
    ]
    (PASTA_SQL / "07b_reverter_questoes_codigo.sql").write_text("\n".join(reverter), encoding="utf-8")
    print(f"OK: {PASTA_SQL / '07b_migracao_questoes_codigo.sql'} e 07b_reverter_questoes_codigo.sql ({n} questões)")


if __name__ == "__main__":
    main()
