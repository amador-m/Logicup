-- =====================================================================
-- LogiCup — Migração 07a (Sprint 9, ADR-001): novo formato de resposta CODIGO
--
-- O QUE FAZ: acrescenta o valor 'CODIGO' AO FINAL da lista do ENUM exercicio.formato_resposta.
-- O QUE NÃO FAZ: não altera NENHUMA linha. As 110 questões continuam com o formato que têm.
--   (A troca das 9 questões de código é a migração 07b, aplicada junto com o editor no frontend.)
--
-- POR QUE É SEGURA:
--   - acrescentar um valor no FINAL de um ENUM não muda os valores existentes: o MySQL guarda o
--     ENUM como um número (1 = ESCOLHA_UNICA, 2 = ORDENACAO...) e os números antigos continuam iguais;
--   - no MySQL 8 essa mudança é só de metadados (não reescreve a tabela);
--   - o backend atual (sem o CODIGO no Java) continua funcionando com o banco migrado, e o backend
--     novo também funciona com o banco antigo (o Hibernate não confere os valores do ENUM).
--
-- COMO VOLTAR ATRÁS (só se NENHUMA questão estiver usando CODIGO; o 1º SELECT abaixo confere):
--   ALTER TABLE exercicio MODIFY formato_resposta
--     ENUM('ESCOLHA_UNICA','ORDENACAO','ASSOCIACAO','RESPOSTA_CURTA','TEXTO_LIVRE','BLOCOS') NOT NULL;
-- =====================================================================
USE Logicup;
SET NAMES utf8mb4;

-- ANTES: a lista atual (esperado: ... 'TEXTO_LIVRE','BLOCOS')
SELECT COLUMN_TYPE AS antes FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'exercicio' AND COLUMN_NAME = 'formato_resposta';

ALTER TABLE exercicio MODIFY formato_resposta
  ENUM('ESCOLHA_UNICA','ORDENACAO','ASSOCIACAO','RESPOSTA_CURTA','TEXTO_LIVRE','BLOCOS','CODIGO') NOT NULL;

-- DEPOIS: confere a lista nova e que NENHUMA questão mudou de formato
SELECT COLUMN_TYPE AS depois FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'Logicup' AND TABLE_NAME = 'exercicio' AND COLUMN_NAME = 'formato_resposta';
-- esperado: ESCOLHA_UNICA 69 · TEXTO_LIVRE 13 · RESPOSTA_CURTA 12 · ASSOCIACAO 9 · ORDENACAO 7 (igual a antes)
SELECT formato_resposta, COUNT(*) AS questoes FROM exercicio GROUP BY formato_resposta ORDER BY questoes DESC;
