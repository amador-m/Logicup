-- =====================================================================
-- LogiCup — Migração 10 (Sprint 10, 04/11/2026): confirmação de e-mail, recuperação de senha
-- e preparação do "Entrar com Google".
--
-- O QUE FAZ:
--   usuario.email_confirmado_em  (NOVA, DATETIME NULL)   quando a pessoa clicou no link de confirmação.
--                                                          NULL = ainda não confirmou -> não consegue entrar.
--   usuario.google_id            (NOVA, VARCHAR(255) NULL, ÚNICA)  identificador fixo da pessoa no Google
--                                                          ("sub" do OpenID Connect). Usado no login com Google.
--   usuario.senha                (passa a aceitar NULL)   conta criada só pelo Google não tem senha; ela pode
--                                                          criar uma depois pelo "Esqueci minha senha".
--   token_usuario                (NOVA TABELA)            códigos dos links enviados por e-mail. Guarda só o
--                                                          HASH (SHA-256) do código, nunca o código em si.
--   Contas que JÁ EXISTEM são marcadas como confirmadas (com a data de cadastro), para ninguém ficar
--   trancado do lado de fora: elas foram criadas antes de existir a confirmação.
--
-- O QUE NÃO FAZ: não apaga nem altera nenhum outro dado (progresso, XP, turmas, conquistas).
--
-- ATENÇÃO: ALTER/CREATE TABLE são DDL. No MySQL eles fazem COMMIT sozinhos (não dá ROLLBACK).
--   Por isso este script é IDEMPOTENTE no que importa: se a coluna/tabela já existir, ele PARA no erro
--   ("Duplicate column name" / "Table already exists") sem ter alterado nada daquele comando.
--   Para voltar atrás: 10_reverter_contas_email_google.sql.
--
-- COMO RODAR:
--   - desenvolvimento: no Workbench, execute o arquivo inteiro e confira as duas consultas do final;
--   - produção: DEPLOY.md, seção "Migrações" (faça o backup antes: sudo bash backup.sh).
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;

ALTER TABLE usuario
  MODIFY `senha` varchar(255) DEFAULT NULL COMMENT 'BCrypt; NULL = conta só com o Google (sem senha própria)',
  ADD COLUMN `email_confirmado_em` datetime DEFAULT NULL COMMENT 'NULL = e-mail ainda não confirmado (não entra)',
  ADD COLUMN `google_id` varchar(255) DEFAULT NULL COMMENT 'sub do Google (OpenID Connect); NULL = não usa o Google',
  ADD UNIQUE KEY `uk_usuario_google_id` (`google_id`);

-- contas existentes: confirmadas (foram criadas antes de a confirmação existir)
UPDATE usuario SET email_confirmado_em = COALESCE(data_cadastro, NOW()) WHERE email_confirmado_em IS NULL;

CREATE TABLE `token_usuario` (
  `id_token` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `finalidade` enum('CONFIRMAR_EMAIL','REDEFINIR_SENHA') NOT NULL,
  `hash_token` char(64) NOT NULL COMMENT 'SHA-256 (hex) do código enviado por e-mail; o código em si nunca é gravado',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expira_em` datetime NOT NULL,
  `usado_em` datetime DEFAULT NULL COMMENT 'preenchido quando o link é usado (cada link vale uma vez)',
  PRIMARY KEY (`id_token`),
  UNIQUE KEY `uk_token_usuario_hash` (`hash_token`),
  KEY `idx_token_usuario_finalidade` (`id_usuario`,`finalidade`),
  CONSTRAINT `fk_token_usuario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- CONFERÊNCIA (as duas precisam bater)
SELECT COUNT(*) AS usuarios, SUM(email_confirmado_em IS NOT NULL) AS confirmados FROM usuario;   -- iguais
SELECT COUNT(*) AS colunas_novas FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'usuario'
   AND COLUMN_NAME IN ('email_confirmado_em', 'google_id');                                        -- 2
