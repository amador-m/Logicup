-- LogiCup — "Impressão digital" da ESTRUTURA do banco (só leitura; não altera nada).
-- Serve para comparar dois bancos: se o hash de cada tabela for igual, a estrutura é igual
-- (colunas, tipos, NULL, padrões, chaves, FKs com regras de exclusão e CHECKs).
-- A ordem das colunas é ignorada de propósito (ALTER TABLE ... ADD COLUMN muda só a posição).
USE Logicup;
SET SESSION group_concat_max_len = 1000000;
SELECT t.TABLE_NAME AS tabela,
  MD5(CONCAT(
    (SELECT GROUP_CONCAT(CONCAT_WS(':', c.COLUMN_NAME, c.COLUMN_TYPE, c.IS_NULLABLE,
                         IFNULL(c.COLUMN_DEFAULT, '-'), c.EXTRA) ORDER BY c.COLUMN_NAME SEPARATOR '|')
       FROM information_schema.COLUMNS c
      WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA AND c.TABLE_NAME = t.TABLE_NAME),
    '#',
    IFNULL((SELECT GROUP_CONCAT(CONCAT_WS(':', s.INDEX_NAME, s.NON_UNIQUE, s.SEQ_IN_INDEX, s.COLUMN_NAME)
                                ORDER BY s.INDEX_NAME, s.SEQ_IN_INDEX SEPARATOR '|')
              FROM information_schema.STATISTICS s
             WHERE s.TABLE_SCHEMA = t.TABLE_SCHEMA AND s.TABLE_NAME = t.TABLE_NAME), ''),
    '#',
    IFNULL((SELECT GROUP_CONCAT(CONCAT_WS(':', k.COLUMN_NAME, k.REFERENCED_TABLE_NAME, k.REFERENCED_COLUMN_NAME,
                                          r.DELETE_RULE) ORDER BY k.COLUMN_NAME SEPARATOR '|')
              FROM information_schema.KEY_COLUMN_USAGE k
              JOIN information_schema.REFERENTIAL_CONSTRAINTS r
                ON r.CONSTRAINT_SCHEMA = k.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME = k.CONSTRAINT_NAME
             WHERE k.TABLE_SCHEMA = t.TABLE_SCHEMA AND k.TABLE_NAME = t.TABLE_NAME), ''),
    '#',
    IFNULL((SELECT GROUP_CONCAT(cc.CHECK_CLAUSE ORDER BY cc.CHECK_CLAUSE SEPARATOR '|')
              FROM information_schema.TABLE_CONSTRAINTS tc
              JOIN information_schema.CHECK_CONSTRAINTS cc
                ON cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
             WHERE tc.TABLE_SCHEMA = t.TABLE_SCHEMA AND tc.TABLE_NAME = t.TABLE_NAME
               AND tc.CONSTRAINT_TYPE = 'CHECK'), '')
  )) AS impressao
FROM information_schema.TABLES t
WHERE t.TABLE_SCHEMA = 'Logicup'
ORDER BY t.TABLE_NAME;
