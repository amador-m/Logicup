-- =====================================================================
-- LogiCup — Migração 11 (Sprint 10, 04/11/2026): remove as tabelas recompensa e historico_recompensa.
--
-- POR QUÊ: elas são do modelo inicial ("badges por pontos") e NUNCA foram usadas. Nenhuma classe Java,
--   tela ou script lê ou grava nelas. Na Sprint 9, as recompensas viraram as CONQUISTAS
--   (tabela aluno_conquista) e as GEMAS (aluno.gemas). Tabela sem uso confunde quem lê o modelo e precisa
--   ser explicada na banca.
--
-- ANTES DE RODAR: as duas tabelas precisam estar VAZIAS. Rode só as duas linhas abaixo e confira se dão 0:
--   SELECT COUNT(*) FROM Logicup.recompensa;
--   SELECT COUNT(*) FROM Logicup.historico_recompensa;
--   Se alguma der mais que 0, PARE e fale com a equipe (alguém usou fora do sistema).
--
-- ATENÇÃO: DROP TABLE é DDL (não tem ROLLBACK). Para voltar: 11_reverter_remover_recompensa.sql
--   (recria as tabelas VAZIAS, com a mesma estrutura). Em produção, faça o backup antes (sudo bash backup.sh).
-- =====================================================================
USE Logicup;

-- a ordem importa: historico_recompensa tem FK para recompensa
DROP TABLE historico_recompensa;
DROP TABLE recompensa;

-- CONFERÊNCIA: tem que dar 0
SELECT COUNT(*) AS tabelas_restantes FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME IN ('recompensa', 'historico_recompensa');
