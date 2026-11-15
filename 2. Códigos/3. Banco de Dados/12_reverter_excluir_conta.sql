-- LogiCup — desfaz a migração 12: tira EXCLUIR_CONTA do ENUM de token_usuario.finalidade.
-- Primeiro APAGA os códigos de exclusão que existirem (o MySQL recusaria o ALTER com eles na tabela).
-- Esses códigos só servem para o link de exclusão: apagá-los só faz os links pendentes pararem de funcionar.
USE Logicup;

SET SQL_SAFE_UPDATES = 0;
DELETE FROM token_usuario WHERE finalidade = 'EXCLUIR_CONTA';
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE token_usuario
  MODIFY `finalidade` enum('CONFIRMAR_EMAIL','REDEFINIR_SENHA') NOT NULL;

-- CONFERÊNCIA: tem que mostrar enum('CONFIRMAR_EMAIL','REDEFINIR_SENHA')
SELECT COLUMN_TYPE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'token_usuario' AND COLUMN_NAME = 'finalidade';
