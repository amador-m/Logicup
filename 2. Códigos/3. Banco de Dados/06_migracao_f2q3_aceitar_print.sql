-- =====================================================================
-- LogiCup — 06_migracao_f2q3_aceitar_print.sql (Sprint 9)
--
-- O QUE MUDA: só o gabarito da F2.Q3 ("Complete o Olá Mundo"), que passa a aceitar também
--   System.out.print (antes só System.out.println). O enunciado pede para "exibir" a mensagem e
--   não exige a quebra de linha, então as duas respostas mostram o mesmo conhecimento.
-- O QUE NÃO MUDA: estrutura, outras questões, progresso e XP dos alunos.
-- POR QUE UPDATE (e não recriar o banco): o banco já tem alunos; o id_exercicio continua o mesmo.
-- O 02_seed_conteudo.sql novo (gerado com ajustes_seed.py) já sai com essa mudança.
--
-- COMO EXECUTAR: execute tudo, confira a CONFERÊNCIA e então execute só  COMMIT;  (ou ROLLBACK;).
-- =====================================================================
USE Logicup;

-- ANTES (só leitura): esperado ["System.out.println"]
SELECT codigo_questao, JSON_EXTRACT(gabarito_json, '$.aceitas') AS aceitas_antes
  FROM exercicio WHERE codigo_questao = 'F2.Q3';

START TRANSACTION;

UPDATE exercicio
   SET gabarito_json = '{"aceitas": ["System.out.println", "System.out.print"], "diferenciarMaiusculas": true}'
 WHERE codigo_questao = 'F2.Q3';   -- codigo_questao é UNIQUE: no máximo 1 linha

-- CONFERÊNCIA — esperado: 1 linha, ["System.out.println", "System.out.print"]
SELECT codigo_questao, JSON_EXTRACT(gabarito_json, '$.aceitas') AS aceitas_depois,
       JSON_EXTRACT(gabarito_json, '$.diferenciarMaiusculas') AS diferencia_maiusculas
  FROM exercicio WHERE codigo_questao = 'F2.Q3';

-- DECISÃO (execute SÓ UMA):
-- COMMIT;
-- ROLLBACK;

-- DESFAZER DEPOIS DO COMMIT (só se precisar):
-- UPDATE exercicio SET gabarito_json = '{"aceitas": ["System.out.println"], "diferenciarMaiusculas": true}'
--  WHERE codigo_questao = 'F2.Q3';
