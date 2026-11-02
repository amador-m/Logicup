-- =====================================================================
-- LogiCup — DESFAZ a migração 07b: as 9 questões voltam EXATAMENTE como estavam (a partir do backup).
-- Use só se precisar voltar à correção por palavras-chave (ex.: problema com o executor na banca).
-- O progresso dos alunos não é alterado.
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE exercicio e
  JOIN exercicio_backup_s9 b ON b.id_exercicio = e.id_exercicio
   SET e.formato_resposta = b.formato_resposta,
       e.alternativas_json = b.alternativas_json,
       e.gabarito_json = b.gabarito_json;

-- CONFERÊNCIA: as 9 linhas iguais ao backup (esperado: 9 iguais) e nenhuma questão CODIGO
SELECT SUM(e.formato_resposta = b.formato_resposta
           AND e.gabarito_json = b.gabarito_json
           AND (e.alternativas_json <=> b.alternativas_json)) AS iguais_ao_backup,
       (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'CODIGO') AS ainda_codigo
  FROM exercicio e JOIN exercicio_backup_s9 b ON b.id_exercicio = e.id_exercicio;

-- Se iguais_ao_backup = 9 e ainda_codigo = 0:  COMMIT;
-- Se não:                                      ROLLBACK;
