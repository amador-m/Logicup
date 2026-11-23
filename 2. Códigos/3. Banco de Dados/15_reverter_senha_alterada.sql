-- LogiCup — desfaz a migração 15: tira a coluna usuario.senha_alterada_em.
-- O backend da Sprint 11 (com a revogação) NÃO sobe sem ela: volte também o código.
USE Logicup;

ALTER TABLE usuario DROP COLUMN `senha_alterada_em`;

-- CONFERÊNCIA: tem que dar 0
SELECT COUNT(*) AS coluna FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'senha_alterada_em';
