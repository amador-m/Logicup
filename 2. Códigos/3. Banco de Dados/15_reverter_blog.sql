-- LogiCup — desfaz a migração 15: apaga a tabela post_blog.
-- ATENÇÃO: apaga TODOS os posts, inclusive os publicados e as edições feitas pelo admin.
USE Logicup;

DROP TABLE IF EXISTS `post_blog`;

-- CONFERÊNCIA: tem que dar 0
SELECT COUNT(*) AS tabela FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'post_blog';
