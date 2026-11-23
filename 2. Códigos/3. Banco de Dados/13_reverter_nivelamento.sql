-- LogiCup — desfaz a migração 13: apaga as tabelas do nivelamento.
-- ATENÇÃO: os alunos que já fizeram o nivelamento perdem as fases liberadas por ele
-- (o progresso real deles nas rodadas não é afetado).
USE Logicup;

DROP TABLE IF EXISTS nivelamento;
DROP TABLE IF EXISTS questao_nivelamento;

-- CONFERÊNCIA: tem que dar 0
SELECT COUNT(*) AS tabelas_restantes FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME IN ('nivelamento', 'questao_nivelamento');
