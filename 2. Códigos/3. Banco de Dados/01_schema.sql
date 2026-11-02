-- =====================================================================
-- LogiCup — 01_schema.sql: ESTRUTURA COMPLETA do banco, do zero (sem dados)
--
-- DE ONDE VEIO: o ScriptBD.sql e o Script2BD.sql são um HISTÓRICO de migrações (cria tabela,
--   depois ALTER, depois mais ALTER...). Para recriar o banco do zero, ou criar o banco de
--   produção, precisamos do RESULTADO final dessas migrações. Este arquivo é exatamente isso:
--   as migrações foram executadas em ordem num MySQL 8.0.46 limpo e a estrutura final foi
--   exportada (mysqldump --no-data). As migrações antigas ficam em historico/.
--
-- COMO CONFERIR QUE É IGUAL AO SEU BANCO ATUAL: rode 00_impressao_digital_schema.sql nos
--   dois bancos; os hashes de cada tabela precisam ser iguais (ver README.md).
--
-- ATENÇÃO: o backend usa spring.jpa.hibernate.ddl-auto=validate. Ele NÃO cria nem altera
--   tabelas; só confere se as entidades batem com este schema. Toda mudança de estrutura
--   deve ser feita aqui (e numa migração separada para bancos que já existem).
--
-- COMO EXECUTAR: abra no MySQL Workbench e execute o arquivo inteiro (raio).
--   CREATE TABLE é DDL (commit automático, sem ROLLBACK). Se o banco Logicup já existir com
--   tabelas, o script PARA no primeiro CREATE TABLE ("Table already exists"): isso é proposital.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS Logicup CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE Logicup;
SET NAMES utf8mb4;

-- As tabelas estão em ordem alfabética; algumas têm FK para tabelas criadas depois.
-- Desligar a checagem de FK só durante a criação permite essa ordem (não há dados para checar).
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `aceite_documento` (
  `id_aceite` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `documento` enum('TERMOS','PRIVACIDADE') NOT NULL COMMENT 'TERMOS = aceite; PRIVACIDADE = declaração de ciência',
  `versao` varchar(20) NOT NULL COMMENT 'versão vigente no servidor no momento do aceite',
  `aceito_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_aceite`),
  UNIQUE KEY `uk_aceite_usuario_documento_versao` (`id_usuario`,`documento`,`versao`),
  CONSTRAINT `fk_aceite_documento_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `aluno` (
  `id_usuario` int NOT NULL,
  `nivel_atual` int NOT NULL DEFAULT '1',
  `pontuacao_acumulada` int NOT NULL DEFAULT '0',
  `streak_atual` int NOT NULL DEFAULT '0',
  `gemas` int NOT NULL DEFAULT '0',
  `ultima_atividade_data` date DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `aluno_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `ck_aluno_gemas_nao_negativas` CHECK ((`gemas` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `aluno_avatar` (
  `id_usuario` int NOT NULL,
  `codigo_avatar` varchar(30) NOT NULL,
  `preco_pago` int NOT NULL,
  `comprado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`,`codigo_avatar`),
  CONSTRAINT `fk_aluno_avatar_aluno` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `ck_aluno_avatar_preco` CHECK ((`preco_pago` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `aluno_conquista` (
  `id_usuario` int NOT NULL,
  `codigo_conquista` varchar(40) NOT NULL,
  `nivel` enum('UNICA','BRONZE','PRATA','OURO') NOT NULL,
  `gemas_recebidas` int NOT NULL,
  `desbloqueada_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vista_em` datetime DEFAULT NULL,
  PRIMARY KEY (`id_usuario`,`codigo_conquista`,`nivel`),
  CONSTRAINT `fk_aluno_conquista_aluno` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `ck_aluno_conquista_gemas` CHECK ((`gemas_recebidas` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `aluno_turma` (
  `id_usuario` int NOT NULL,
  `id_turma` int NOT NULL,
  `status` enum('ATIVO','SAIU','REMOVIDO') NOT NULL DEFAULT 'ATIVO',
  `entrou_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime DEFAULT NULL,
  PRIMARY KEY (`id_usuario`,`id_turma`),
  KEY `id_turma` (`id_turma`),
  CONSTRAINT `aluno_turma_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `aluno_turma_ibfk_2` FOREIGN KEY (`id_turma`) REFERENCES `turma` (`id_turma`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dica` (
  `id_dica` int NOT NULL AUTO_INCREMENT,
  `id_exercicio` int NOT NULL,
  `texto_dica` varchar(255) NOT NULL,
  PRIMARY KEY (`id_dica`),
  KEY `idx_dica_exercicio` (`id_exercicio`),
  CONSTRAINT `dica_ibfk_1` FOREIGN KEY (`id_exercicio`) REFERENCES `exercicio` (`id_exercicio`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `exercicio` (
  `id_exercicio` int NOT NULL AUTO_INCREMENT,
  `id_rodada` int NOT NULL,
  `codigo_questao` varchar(20) NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `ordenacao` int NOT NULL,
  `dificuldade` enum('FACIL','MEDIO','DIFICIL') DEFAULT NULL,
  `tipo_exercicio` enum('VERDADEIRO_FALSO','MULTIPLA_ESCOLHA','BLOCOS','COMPLETAR_CODIGO','ENCONTRAR_ERRO','PREVER_SAIDA','ESCREVER_CODIGO','ASSOCIACAO','DESAFIO_RELAMPAGO','PUZZLE_LOGICO','ORDEM_CORRETA','BOSS_CHALLENGE') NOT NULL,
  `formato_resposta` enum('ESCOLHA_UNICA','ORDENACAO','ASSOCIACAO','RESPOSTA_CURTA','TEXTO_LIVRE','BLOCOS','CODIGO') NOT NULL,
  `pontos` int NOT NULL,
  `enunciado` text NOT NULL,
  `codigo_apoio` text,
  `alternativas_json` json DEFAULT NULL,
  `gabarito_json` json NOT NULL,
  `explicacao` text NOT NULL,
  `template_inicial_java` text,
  `estrutura_blocos_json` json DEFAULT NULL,
  `codigo_teste_junit` text,
  `habilidade` varchar(255) DEFAULT NULL COMMENT 'Ementa (S7): o que o aluno desenvolve com a questão',
  PRIMARY KEY (`id_exercicio`),
  UNIQUE KEY `uk_exercicio_codigo` (`codigo_questao`),
  UNIQUE KEY `uk_exercicio_rodada_ordem` (`id_rodada`,`ordenacao`),
  CONSTRAINT `fk_exercicio_rodada` FOREIGN KEY (`id_rodada`) REFERENCES `rodada` (`id_rodada`) ON DELETE CASCADE,
  CONSTRAINT `chk_exercicio_pontos` CHECK ((`pontos` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `historico_recompensa` (
  `id_historico` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `id_recompensa` int NOT NULL,
  `data_ganho` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_historico`),
  UNIQUE KEY `uk_aluno_recompensa` (`id_usuario`,`id_recompensa`),
  KEY `id_recompensa` (`id_recompensa`),
  CONSTRAINT `historico_recompensa_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `historico_recompensa_ibfk_2` FOREIGN KEY (`id_recompensa`) REFERENCES `recompensa` (`id_recompensa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `modulo` (
  `id_modulo` int NOT NULL AUTO_INCREMENT,
  `titulo` varchar(150) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `ordenacao` int NOT NULL,
  PRIMARY KEY (`id_modulo`),
  UNIQUE KEY `uk_modulo_ordenacao` (`ordenacao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `progresso_exercicio` (
  `id_progresso` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `id_exercicio` int NOT NULL,
  `status` enum('EM_ANDAMENTO','CONCLUIDO') NOT NULL DEFAULT 'EM_ANDAMENTO',
  `tentativas` int NOT NULL DEFAULT '0',
  `pontos_ganhos` int NOT NULL DEFAULT '0',
  `resposta_json` json DEFAULT NULL,
  `data_resposta` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_progresso`),
  UNIQUE KEY `uk_aluno_exercicio` (`id_usuario`,`id_exercicio`),
  KEY `id_exercicio` (`id_exercicio`),
  CONSTRAINT `progresso_exercicio_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `aluno` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `progresso_exercicio_ibfk_2` FOREIGN KEY (`id_exercicio`) REFERENCES `exercicio` (`id_exercicio`) ON DELETE CASCADE,
  CONSTRAINT `chk_progresso_valores` CHECK (((`tentativas` >= 0) and (`pontos_ganhos` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recompensa` (
  `id_recompensa` int NOT NULL AUTO_INCREMENT,
  `nome_recompensa` varchar(150) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `criterio_pontos` int NOT NULL,
  `imagem_badge_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_recompensa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `rodada` (
  `id_rodada` int NOT NULL AUTO_INCREMENT,
  `id_modulo` int NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `ordenacao` int NOT NULL,
  `conteudo_teorico` json NOT NULL,
  PRIMARY KEY (`id_rodada`),
  UNIQUE KEY `uk_rodada_modulo_ordem` (`id_modulo`,`ordenacao`),
  CONSTRAINT `fk_rodada_modulo` FOREIGN KEY (`id_modulo`) REFERENCES `modulo` (`id_modulo`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tarefa` (
  `id_tarefa` int NOT NULL AUTO_INCREMENT,
  `id_turma` int NOT NULL,
  `id_rodada` int NOT NULL,
  `prazo` date DEFAULT NULL COMMENT 'informativo: depois dele a tarefa aparece como atrasada',
  `criada_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_tarefa`),
  UNIQUE KEY `uk_tarefa_turma_rodada` (`id_turma`,`id_rodada`),
  KEY `fk_tarefa_rodada` (`id_rodada`),
  CONSTRAINT `fk_tarefa_rodada` FOREIGN KEY (`id_rodada`) REFERENCES `rodada` (`id_rodada`) ON DELETE CASCADE,
  CONSTRAINT `fk_tarefa_turma` FOREIGN KEY (`id_turma`) REFERENCES `turma` (`id_turma`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `turma` (
  `id_turma` int NOT NULL AUTO_INCREMENT,
  `nome_turma` varchar(100) NOT NULL,
  `codigo_convite` char(8) NOT NULL,
  `id_professor` int DEFAULT NULL,
  `criada_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_turma`),
  UNIQUE KEY `uk_turma_codigo_convite` (`codigo_convite`),
  KEY `idx_turma_professor` (`id_professor`),
  CONSTRAINT `turma_ibfk_1` FOREIGN KEY (`id_professor`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `username` varchar(20) NOT NULL,
  `e_mail` varchar(150) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `tipo_usuario` enum('ALUNO','PROFESSOR','ADMIN') NOT NULL,
  `avatar` varchar(30) NOT NULL DEFAULT 'GATO_CLARO',
  `data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `e_mail` (`e_mail`),
  UNIQUE KEY `uk_usuario_username` (`username`),
  KEY `idx_usuario_email` (`e_mail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



SET FOREIGN_KEY_CHECKS = 1;
