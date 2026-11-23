-- =====================================================================
-- LogiCup — Migração 14 (Sprint 11, 17/11/2026): BLOQUEIO DE CONTA pelo administrador.
--
-- POR QUÊ: o artigo e o RF25 preveem o "gerenciamento de usuários por administradores". Bloquear é a
--   ação escolhida pela equipe: a conta deixa de entrar (e quem já estava logado perde o acesso na hora),
--   mas NADA é apagado. Desbloquear devolve tudo como estava.
--
-- COLUNA NOVA: usuario.bloqueado_em (DATETIME, NULL = conta ativa). Guardar a DATA, e não só sim/não,
--   registra QUANDO a conta foi bloqueada, sem precisar de outra coluna.
--
-- O que NÃO muda: nenhuma linha (todo mundo começa ativo).
-- ATENÇÃO: ALTER TABLE é DDL (sem ROLLBACK). Para voltar: 14_reverter_bloqueio_usuario.sql.
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;

ALTER TABLE usuario
  ADD COLUMN `bloqueado_em` datetime DEFAULT NULL COMMENT 'Sprint 11 (migração 14): bloqueada pelo admin; NULL = ativa' AFTER `google_id`;

-- CONFERÊNCIA: tem que mostrar 1 coluna nova e 0 contas bloqueadas
SELECT COUNT(*) AS coluna_nova FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'bloqueado_em';
SELECT COUNT(*) AS contas_bloqueadas FROM usuario WHERE bloqueado_em IS NOT NULL;
