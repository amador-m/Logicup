-- LogiCup — desfaz a migração 11: recria recompensa e historico_recompensa VAZIAS (mesma estrutura de antes).
USE Logicup;

CREATE TABLE `recompensa` (
  `id_recompensa` int NOT NULL AUTO_INCREMENT,
  `nome_recompensa` varchar(150) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `criterio_pontos` int NOT NULL,
  `imagem_badge_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_recompensa`)
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
