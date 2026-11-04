-- =====================================================================
-- LogiCup — DESFAZ a migração 10. Use SÓ junto com a volta do backend para a versão anterior.
--
-- ATENÇÃO:
--   - apaga a tabela token_usuario (links pendentes deixam de funcionar; não há outra perda);
--   - remove email_confirmado_em e google_id (quem entrou pelo Google perde o vínculo com o Google);
--   - senha volta a ser obrigatória: se existir conta SEM senha (criada só pelo Google), o último ALTER
--     FALHA de propósito. A 1ª consulta mostra quantas são; resolva antes (ex.: pedir que criem senha).
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;

SELECT COUNT(*) AS contas_sem_senha FROM usuario WHERE senha IS NULL;   -- precisa ser 0

DROP TABLE token_usuario;

ALTER TABLE usuario
  DROP INDEX `uk_usuario_google_id`,
  DROP COLUMN `google_id`,
  DROP COLUMN `email_confirmado_em`,
  MODIFY `senha` varchar(255) NOT NULL;
