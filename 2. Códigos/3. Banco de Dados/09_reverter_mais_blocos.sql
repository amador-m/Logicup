-- =====================================================================
-- LogiCup — DESFAZ a migração 09: as 5 questões voltam EXATAMENTE como estavam (ORDENAÇÃO de linhas),
-- a partir do backup exercicio_backup_0311_blocos. O progresso dos alunos não é alterado. A F5.Q8 não é tocada.
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE exercicio e
  JOIN exercicio_backup_0311_blocos b ON b.id_exercicio = e.id_exercicio
   SET e.tipo_exercicio = b.tipo_exercicio,
       e.formato_resposta = b.formato_resposta,
       e.enunciado = b.enunciado,
       e.explicacao = b.explicacao,
       e.habilidade = b.habilidade,
       e.alternativas_json = b.alternativas_json,
       e.gabarito_json = b.gabarito_json;

-- CONFERÊNCIA: as linhas iguais ao backup (esperado: 5) e só a F5.Q8 em BLOCOS
SELECT SUM(e.tipo_exercicio = b.tipo_exercicio AND e.formato_resposta = b.formato_resposta
           AND e.enunciado = b.enunciado AND e.explicacao = b.explicacao
           AND (e.habilidade <=> b.habilidade) AND e.gabarito_json = b.gabarito_json
           AND (e.alternativas_json <=> b.alternativas_json)) AS iguais_ao_backup,
       (SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'BLOCOS') AS em_blocos
  FROM exercicio e JOIN exercicio_backup_0311_blocos b ON b.id_exercicio = e.id_exercicio;

-- Se iguais_ao_backup = 5 e em_blocos = 1:  COMMIT;
-- Se não:                                   ROLLBACK;
