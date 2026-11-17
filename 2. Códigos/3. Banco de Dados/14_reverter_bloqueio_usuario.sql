-- LogiCup — desfaz a migração 14: remove usuario.bloqueado_em.
-- ATENÇÃO: contas que estavam bloqueadas voltam a entrar.
USE Logicup;

ALTER TABLE usuario DROP COLUMN `bloqueado_em`;

-- CONFERÊNCIA: tem que dar 0
SELECT COUNT(*) AS coluna FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'bloqueado_em';
