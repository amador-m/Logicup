-- =====================================================================
-- LogiCup — Migração 15 (Sprint 11, 23/11/2026): ENCERRAR AS SESSÕES ANTIGAS QUANDO A SENHA MUDA.
--
-- POR QUÊ: o login é um token (JWT) assinado, válido por 8 horas. Antes, trocar a senha não derrubava os
--   tokens que já existiam: quem tivesse roubado a sessão continuava entrando até o token vencer.
--   Agora a conta guarda QUANDO a senha mudou, e o backend recusa qualquer token emitido ANTES disso.
--
-- COLUNA NOVA: usuario.senha_alterada_em (DATETIME, NULL = a senha nunca foi trocada depois desta migração).
--
-- O que NÃO muda: nenhuma linha (todo mundo começa com NULL; nenhuma sessão atual é derrubada).
-- ATENÇÃO: ALTER TABLE é DDL (sem ROLLBACK). Para voltar: 15_reverter_senha_alterada.sql.
-- (O blog, que estava planejado como migração 15, foi adiado e vai virar a 16.)
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;

ALTER TABLE usuario
  ADD COLUMN `senha_alterada_em` datetime DEFAULT NULL COMMENT 'Sprint 11 (migração 15): tokens emitidos antes disto são recusados' AFTER `bloqueado_em`;

-- CONFERÊNCIA: tem que mostrar 1 coluna nova
SELECT COUNT(*) AS coluna_nova FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'senha_alterada_em';
