-- =====================================================================
-- LogiCup — Migração 13 (Sprint 11, 16/11/2026): QUIZ DE NIVELAMENTO.
--
-- O QUE É: no primeiro acesso, o aluno pode fazer um teste curto (10 questões, 2 por fase, Fases 1 a 5).
--   Cada fase em que ele acerta AS DUAS questões conta como "dominada". As fases dominadas em sequência,
--   a partir da Fase 1, ficam liberadas: ele pode começar mais adiante na trilha.
--   Liberar NÃO dá XP, gemas nem conquistas: isso só vem fazendo as rodadas de verdade.
--
-- TABELAS NOVAS:
--   questao_nivelamento: as 10 questões do teste (separadas dos exercícios da trilha: responder o
--     nivelamento não gera progresso na trilha);
--   nivelamento: 1 linha por aluno, gravada UMA vez (fez o teste ou escolheu começar do zero).
--     A chave primária é o próprio id do aluno: o banco impede um segundo registro.
--     ON DELETE CASCADE: apagar a conta apaga o nivelamento (como todo o resto do aluno).
--
-- O que NÃO muda: nenhuma tabela existente; o progresso de quem já usa a plataforma continua igual
--   (o nivelamento só é oferecido a quem ainda não tem nenhum progresso).
--
-- Também serve para instalação nova: rode depois do 01_schema.sql e do 02_seed_conteudo.sql
--   (CREATE TABLE IF NOT EXISTS; as questões só entram se a tabela estiver vazia).
-- ATENÇÃO: CREATE TABLE é DDL (sem ROLLBACK). Para voltar: 13_reverter_nivelamento.sql.
-- =====================================================================
USE Logicup;
-- acentos: o arquivo é UTF-8; sem isto, um cliente configurado em latin1 gravaria "Ã©" no lugar de "é"
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `questao_nivelamento` (
  `id_questao` int NOT NULL AUTO_INCREMENT,
  `fase` int NOT NULL,                 -- ordenação da fase (modulo.ordenacao) que a questão avalia
  `ordenacao` int NOT NULL,            -- ordem dentro da fase (1 ou 2)
  `enunciado` text NOT NULL,
  `codigo` text DEFAULT NULL,          -- trecho de código mostrado junto do enunciado (opcional)
  `alternativas_json` json NOT NULL,   -- [{"id": "a", "texto": "..."}, ...]
  `correta` varchar(5) NOT NULL,       -- id da alternativa certa (nunca vai para o navegador antes da resposta)
  `explicacao` text NOT NULL,
  PRIMARY KEY (`id_questao`),
  UNIQUE KEY `uk_nivelamento_fase_ordem` (`fase`, `ordenacao`),
  CONSTRAINT `ck_nivelamento_fase` CHECK ((`fase` >= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `nivelamento` (
  `id_usuario` int NOT NULL,
  `fez_teste` tinyint(1) NOT NULL,     -- 0 = escolheu começar do zero
  `acertos` int NOT NULL DEFAULT '0',
  `total` int NOT NULL DEFAULT '0',
  `fases_liberadas` int NOT NULL DEFAULT '0',  -- fases dominadas em sequência desde a Fase 1
  `realizado_em` datetime NOT NULL,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `nivelamento_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `ck_nivelamento_valores` CHECK (((`acertos` >= 0) and (`total` >= `acertos`) and (`fases_liberadas` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- As 10 questões (só entram se a tabela estiver vazia: rodar duas vezes não duplica)
INSERT INTO questao_nivelamento (fase, ordenacao, enunciado, codigo, alternativas_json, correta, explicacao)
SELECT * FROM (
  SELECT 1 AS fase, 1 AS ordenacao,
    'O Gattuccino precisa trocar uma lâmpada queimada. Qual sequência de passos está na ordem lógica correta?' AS enunciado,
    NULL AS codigo,
    '[{"id": "a", "texto": "Desligar o interruptor → Retirar a lâmpada queimada → Colocar a lâmpada nova → Ligar o interruptor"}, {"id": "b", "texto": "Retirar a lâmpada queimada → Desligar o interruptor → Colocar a lâmpada nova → Ligar o interruptor"}, {"id": "c", "texto": "Colocar a lâmpada nova → Retirar a lâmpada queimada → Desligar o interruptor → Ligar o interruptor"}, {"id": "d", "texto": "Ligar o interruptor → Retirar a lâmpada queimada → Colocar a lâmpada nova → Desligar o interruptor"}]' AS alternativas_json,
    'a' AS correta,
    'Um algoritmo é uma sequência de passos em que a ordem importa: por segurança, primeiro se desliga a energia, depois se troca a lâmpada e só no fim se liga de novo. Nas outras opções, algum passo acontece antes daquele de que ele depende.' AS explicacao
  UNION ALL SELECT 1, 2,
    'Dividir um problema grande em partes menores, que podem ser resolvidas uma de cada vez, é uma técnica do pensamento computacional chamada de:',
    NULL,
    '[{"id": "a", "texto": "Compilação"}, {"id": "b", "texto": "Decomposição"}, {"id": "c", "texto": "Depuração"}, {"id": "d", "texto": "Concatenação"}]',
    'b',
    'Decomposição é quebrar um problema em partes menores e mais fáceis de resolver. Compilação é traduzir o código, depuração é procurar e corrigir erros, e concatenação é juntar textos.'
  UNION ALL SELECT 2, 1,
    'O que este trecho de código mostra no console?',
    'System.out.print("Olá, ");\nSystem.out.println("Java");\nSystem.out.println("!");',
    '[{"id": "a", "texto": "Olá, Java! (tudo em uma linha)"}, {"id": "b", "texto": "Olá, Java na primeira linha e ! na segunda linha"}, {"id": "c", "texto": "Olá, / Java / ! (cada um em uma linha)"}, {"id": "d", "texto": "Olá, na primeira linha e Java! na segunda linha"}]',
    'b',
    'O print exibe o texto sem pular de linha, então "Java" aparece logo depois de "Olá, ". O println exibe e depois pula de linha, por isso o "!" vai para a linha seguinte.'
  UNION ALL SELECT 2, 2,
    'Por que este programa NÃO compila?',
    'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Oi")\n    }\n}',
    '[{"id": "a", "texto": "Falta o ponto e vírgula no final da instrução System.out.println"}, {"id": "b", "texto": "Deveria ser System.out.print, e não println"}, {"id": "c", "texto": "O texto deveria estar entre aspas simples"}, {"id": "d", "texto": "Um programa Java não pode ter apenas uma instrução"}]',
    'a',
    'Em Java, toda instrução termina com ponto e vírgula. Sem ele, o compilador acusa erro de sintaxe. println e print são válidos, e textos (String) usam aspas duplas.'
  UNION ALL SELECT 3, 1,
    'O que este trecho exibe?',
    'int a = 7;\nint b = 2;\nSystem.out.println(a / b);',
    '[{"id": "a", "texto": "3.5"}, {"id": "b", "texto": "3"}, {"id": "c", "texto": "4"}, {"id": "d", "texto": "3.0"}]',
    'b',
    'A divisão entre dois valores int é uma divisão inteira: a parte decimal é descartada (não é arredondada). 7 / 2 resulta em 3. Para obter 3.5, pelo menos um dos valores precisaria ser double.'
  UNION ALL SELECT 3, 2,
    'Qual é o valor de x depois desta linha?',
    'int x = 2 + 3 * 4;',
    '[{"id": "a", "texto": "20"}, {"id": "b", "texto": "24"}, {"id": "c", "texto": "14"}, {"id": "d", "texto": "9"}]',
    'c',
    'A multiplicação tem precedência sobre a soma: primeiro 3 * 4 = 12, depois 2 + 12 = 14. Para somar primeiro, seria preciso usar parênteses: (2 + 3) * 4 = 20.'
  UNION ALL SELECT 4, 1,
    'O que este trecho exibe?',
    'int xp = 80;\nif (xp >= 100) {\n    System.out.println("Ouro");\n} else if (xp >= 50) {\n    System.out.println("Prata");\n} else {\n    System.out.println("Bronze");\n}',
    '[{"id": "a", "texto": "Ouro"}, {"id": "b", "texto": "Bronze"}, {"id": "c", "texto": "Prata"}, {"id": "d", "texto": "Prata e Bronze"}]',
    'c',
    'As condições são testadas em ordem e só o primeiro bloco verdadeiro executa. 80 >= 100 é falso; 80 >= 50 é verdadeiro, então aparece "Prata" e o else não é executado.'
  UNION ALL SELECT 4, 2,
    'O que este trecho exibe?',
    'boolean temChave = true;\nint nivel = 3;\nif (temChave && nivel > 5) {\n    System.out.println("Porta aberta");\n} else {\n    System.out.println("Porta fechada");\n}',
    '[{"id": "a", "texto": "Porta fechada"}, {"id": "b", "texto": "Porta aberta"}, {"id": "c", "texto": "Nada é exibido"}, {"id": "d", "texto": "O código não compila"}]',
    'a',
    'O operador && (E) só resulta em verdadeiro se as duas condições forem verdadeiras. temChave é true, mas nivel > 5 é falso (3 não é maior que 5), então a condição inteira é falsa e o else executa.'
  UNION ALL SELECT 5, 1,
    'O que este laço exibe?',
    'for (int i = 1; i <= 3; i++) {\n    System.out.print(i + " ");\n}',
    '[{"id": "a", "texto": "0 1 2"}, {"id": "b", "texto": "1 2"}, {"id": "c", "texto": "1 2 3 4"}, {"id": "d", "texto": "1 2 3"}]',
    'd',
    'O i começa em 1 e o laço repete enquanto i <= 3, somando 1 a cada volta. Ele executa com i = 1, 2 e 3; quando i vale 4, a condição fica falsa e o laço termina.'
  UNION ALL SELECT 5, 2,
    'Qual valor é exibido no final?',
    'int soma = 0;\nint i = 1;\nwhile (i <= 4) {\n    soma += i;\n    i++;\n}\nSystem.out.println(soma);',
    '[{"id": "a", "texto": "4"}, {"id": "b", "texto": "6"}, {"id": "c", "texto": "10"}, {"id": "d", "texto": "15"}]',
    'c',
    'A variável soma é um acumulador: a cada volta ela recebe o valor atual de i. Ela soma 1 + 2 + 3 + 4 = 10. Quando i chega a 5, a condição i <= 4 fica falsa e o laço para.'
) AS novas
WHERE NOT EXISTS (SELECT 1 FROM questao_nivelamento);

-- CONFERÊNCIA: tem que mostrar 10 questões, 2 em cada fase de 1 a 5, e a tabela nivelamento vazia
SELECT fase, COUNT(*) AS questoes FROM questao_nivelamento GROUP BY fase ORDER BY fase;
SELECT COUNT(*) AS registros_nivelamento FROM nivelamento;
