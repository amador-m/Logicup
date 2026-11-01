-- =====================================================================
-- LogiCup — 03_conferencia_seed.sql: VALIDAÇÃO do conteúdo pedagógico (só leitura)
--
-- Rode DEPOIS do 02_seed_conteudo.sql. Não altera nada.
-- Resultado 1 (o principal): uma linha por verificação, com o valor ESPERADO, o ENCONTRADO
--   e a coluna situacao. TODAS as linhas precisam estar "OK".
-- Resultados 2 e 3: resumo por fase/rodada, para conferir com o Banco de Questões.
-- =====================================================================
USE Logicup;

SELECT verificacao, esperado, encontrado, IF(esperado = encontrado, 'OK', 'ERRO') AS situacao
FROM (
    -- ---- Quantidades ----
    SELECT 1 AS n, 'Fases (modulo)' AS verificacao, '10' AS esperado,
           CAST((SELECT COUNT(*) FROM modulo) AS CHAR) AS encontrado
    UNION ALL SELECT 2, 'Rodadas', '20', CAST((SELECT COUNT(*) FROM rodada) AS CHAR)
    UNION ALL SELECT 3, 'Questões', '110', CAST((SELECT COUNT(*) FROM exercicio) AS CHAR)
    UNION ALL SELECT 4, 'XP total da trilha', '2400', CAST((SELECT SUM(pontos) FROM exercicio) AS CHAR)
    UNION ALL SELECT 5, 'Fases numeradas de 1 a 10, sem buraco', '1-10',
           (SELECT CONCAT(MIN(ordenacao), '-', MAX(ordenacao)) FROM modulo)

    -- ---- Integridade e relacionamentos ----
    UNION ALL SELECT 6, 'Fases com exatamente 2 rodadas (1 e 2)', '10',
           CAST((SELECT COUNT(*) FROM (SELECT id_modulo FROM rodada GROUP BY id_modulo
                 HAVING COUNT(*) = 2 AND MIN(ordenacao) = 1 AND MAX(ordenacao) = 2) x) AS CHAR)
    UNION ALL SELECT 7, 'Rodadas sem nenhuma questão', '0',
           CAST((SELECT COUNT(*) FROM rodada r WHERE NOT EXISTS
                 (SELECT 1 FROM exercicio e WHERE e.id_rodada = r.id_rodada)) AS CHAR)
    UNION ALL SELECT 8, 'Questões com posição fora de 1..N (buraco na ordem)', '0',
           CAST((SELECT COUNT(*) FROM (SELECT id_rodada FROM exercicio GROUP BY id_rodada
                 HAVING MIN(ordenacao) <> 1 OR MAX(ordenacao) <> COUNT(*)) x) AS CHAR)
    UNION ALL SELECT 9, 'Códigos de questão repetidos', '0',
           CAST((SELECT COUNT(*) FROM (SELECT codigo_questao FROM exercicio GROUP BY codigo_questao
                 HAVING COUNT(*) > 1) x) AS CHAR)
    UNION ALL SELECT 10, 'Questão na fase errada (código F<n> diferente da fase)', '0',
           CAST((SELECT COUNT(*) FROM exercicio e JOIN rodada r ON r.id_rodada = e.id_rodada
                 JOIN modulo m ON m.id_modulo = r.id_modulo
                 WHERE e.codigo_questao NOT LIKE CONCAT('F', m.ordenacao, '.%')) AS CHAR)
    UNION ALL SELECT 11, 'Fase 9: questão na rodada errada (F9.R<n>)', '0',
           CAST((SELECT COUNT(*) FROM exercicio e JOIN rodada r ON r.id_rodada = e.id_rodada
                 JOIN modulo m ON m.id_modulo = r.id_modulo
                 WHERE m.ordenacao = 9 AND e.codigo_questao NOT LIKE CONCAT('F9.R', r.ordenacao, '.%')) AS CHAR)
    UNION ALL SELECT 12, 'Dicas apontando para questão inexistente', '0',
           CAST((SELECT COUNT(*) FROM dica d LEFT JOIN exercicio e ON e.id_exercicio = d.id_exercicio
                 WHERE e.id_exercicio IS NULL) AS CHAR)

    -- ---- Regras pedagógicas ----
    UNION ALL SELECT 13, 'Fases com XP diferente de 205 (exceto F9 = 410 e F10 = 350)', '0',
           CAST((SELECT COUNT(*) FROM (SELECT m.ordenacao, SUM(e.pontos) xp FROM modulo m
                 JOIN rodada r ON r.id_modulo = m.id_modulo JOIN exercicio e ON e.id_rodada = r.id_rodada
                 GROUP BY m.ordenacao) x
                 WHERE xp <> CASE ordenacao WHEN 9 THEN 410 WHEN 10 THEN 350 ELSE 205 END) AS CHAR)
    UNION ALL SELECT 14, 'Bosses (1 por fase; a Fase 9 tem 2)', '11',
           CAST((SELECT COUNT(*) FROM exercicio WHERE tipo_exercicio = 'BOSS_CHALLENGE') AS CHAR)
    UNION ALL SELECT 15, 'Boss que NÃO é a última questão da rodada', '0',
           CAST((SELECT COUNT(*) FROM exercicio e WHERE e.tipo_exercicio = 'BOSS_CHALLENGE'
                 AND e.ordenacao <> (SELECT MAX(x.ordenacao) FROM exercicio x WHERE x.id_rodada = e.id_rodada)) AS CHAR)
    UNION ALL SELECT 16, 'Questões sem habilidade (ementa do professor)', '0',
           CAST((SELECT COUNT(*) FROM exercicio WHERE habilidade IS NULL OR habilidade = '') AS CHAR)
    UNION ALL SELECT 17, 'Fases sem objetivo geral (modulo.descricao)', '0',
           CAST((SELECT COUNT(*) FROM modulo WHERE descricao IS NULL OR descricao = '') AS CHAR)
    UNION ALL SELECT 18, 'Personagem genérico (Joana/João) em vez do Gattuccino', '0',
           CAST((SELECT COUNT(*) FROM exercicio WHERE enunciado REGEXP 'Joana|João' OR explicacao REGEXP 'Joana|João') AS CHAR)

    -- ---- JSON: formato que o backend/frontend esperam ----
    UNION ALL SELECT 19, 'Teoria (conteudo_teorico) com objetivos e seções', '20',
           CAST((SELECT COUNT(*) FROM rodada WHERE JSON_LENGTH(conteudo_teorico, '$.objetivos') > 0
                 AND JSON_LENGTH(conteudo_teorico, '$.teoria') > 0) AS CHAR)
    UNION ALL SELECT 20, 'ESCOLHA_UNICA: gabarito aponta para alternativa existente',
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ESCOLHA_UNICA') AS CHAR),
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ESCOLHA_UNICA'
                 AND JSON_CONTAINS(JSON_EXTRACT(alternativas_json, '$[*].id'),
                                   JSON_QUOTE(JSON_UNQUOTE(JSON_EXTRACT(gabarito_json, '$.correta'))))) AS CHAR)
    UNION ALL SELECT 21, 'ORDENACAO: gabarito usa todos os itens, uma vez cada',
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ORDENACAO') AS CHAR),
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ORDENACAO'
                 AND JSON_LENGTH(gabarito_json, '$.ordem') = JSON_LENGTH(alternativas_json)
                 AND JSON_CONTAINS(JSON_EXTRACT(alternativas_json, '$[*].id'), JSON_EXTRACT(gabarito_json, '$.ordem'))) AS CHAR)
    UNION ALL SELECT 22, 'ASSOCIACAO: um par para cada item',
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ASSOCIACAO') AS CHAR),
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'ASSOCIACAO'
                 AND JSON_LENGTH(gabarito_json, '$.pares') = JSON_LENGTH(alternativas_json, '$.itens')) AS CHAR)
    UNION ALL SELECT 23, 'RESPOSTA_CURTA: gabarito com "aceitas" ou "lacunas"',
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'RESPOSTA_CURTA') AS CHAR),
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'RESPOSTA_CURTA'
                 AND (JSON_LENGTH(gabarito_json, '$.aceitas') > 0 OR JSON_LENGTH(gabarito_json, '$.lacunas') > 0)) AS CHAR)
    UNION ALL SELECT 24, 'TEXTO_LIVRE: mínimo entre 1 e o nº de grupos',
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'TEXTO_LIVRE') AS CHAR),
           CAST((SELECT COUNT(*) FROM exercicio WHERE formato_resposta = 'TEXTO_LIVRE'
                 AND JSON_EXTRACT(gabarito_json, '$.minimo') BETWEEN 1 AND JSON_LENGTH(gabarito_json, '$.palavrasChave')) AS CHAR)
) verificacoes
ORDER BY n;

-- Resumo por rodada (compare com a linha "Questões: ... · XP da rodada" do documento)
SELECT m.ordenacao AS fase, r.ordenacao AS rodada, r.titulo, COUNT(e.id_exercicio) AS questoes,
       SUM(e.pontos) AS xp, GROUP_CONCAT(e.codigo_questao ORDER BY e.ordenacao SEPARATOR ', ') AS ordem_das_questoes
  FROM modulo m JOIN rodada r ON r.id_modulo = m.id_modulo JOIN exercicio e ON e.id_rodada = r.id_rodada
 GROUP BY m.ordenacao, r.ordenacao, r.titulo
 ORDER BY m.ordenacao, r.ordenacao;

-- Quantidade por formato de resposta (esperado: ESCOLHA_UNICA 69, TEXTO_LIVRE 13, RESPOSTA_CURTA 12,
-- ASSOCIACAO 9, ORDENACAO 7)
SELECT formato_resposta, COUNT(*) AS questoes FROM exercicio GROUP BY formato_resposta ORDER BY 2 DESC;
