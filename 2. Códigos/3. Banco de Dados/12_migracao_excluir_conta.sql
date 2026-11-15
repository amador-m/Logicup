-- =====================================================================
-- LogiCup — Migração 12 (Sprint 10, 15/11/2026): link de EXCLUSÃO DE CONTA por e-mail.
--
-- POR QUÊ: quem entra só com o Google não tem senha para confirmar a exclusão da conta. Essa pessoa
--   recebe um link de uso único por e-mail (vale 30 minutos), guardado na tabela token_usuario como os
--   links de "confirmar e-mail" e "nova senha". A coluna finalidade é um ENUM do MySQL: ela só aceita os
--   valores listados, então o valor novo EXCLUIR_CONTA precisa ser acrescentado aqui.
--
-- O que NÃO muda: nenhuma linha é alterada; os códigos que já existem continuam valendo.
--   A exclusão em si não precisa de migração: todas as tabelas com dados do usuário já têm
--   ON DELETE CASCADE desde o 01_schema.sql.
--
-- ATENÇÃO: ALTER TABLE é DDL (não tem ROLLBACK). Para voltar: 12_reverter_excluir_conta.sql.
--   Em produção, faça o backup antes (sudo bash backup.sh). Aplicar junto com o backend da exclusão de conta.
-- =====================================================================
USE Logicup;

ALTER TABLE token_usuario
  MODIFY `finalidade` enum('CONFIRMAR_EMAIL','REDEFINIR_SENHA','EXCLUIR_CONTA') NOT NULL;

-- CONFERÊNCIA: tem que mostrar enum('CONFIRMAR_EMAIL','REDEFINIR_SENHA','EXCLUIR_CONTA')
SELECT COLUMN_TYPE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'token_usuario' AND COLUMN_NAME = 'finalidade';
