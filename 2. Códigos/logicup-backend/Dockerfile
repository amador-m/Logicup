# =====================================================================
# LogiCup — imagem do BACKEND (deploy, 03/11/2026)
#
# "Multi-stage build": duas etapas na mesma receita.
#   1) build:   imagem com o JDK completo + Maven, que compila e gera o .jar
#   2) final:   imagem só com o JRE (o necessário para RODAR Java) + o .jar
# A imagem final não leva código-fonte, Maven nem compilador: fica menor e com menos coisas
# que um atacante poderia usar.
#
# Os testes NÃO rodam aqui (eles precisam de um MySQL): rode "mvnw test" no seu PC antes do deploy.
# =====================================================================

# ---------- etapa 1: compilar ----------
# Imagem oficial do Maven com o JDK 21 (a mesma versão do pom.xml). Usamos o "mvn" dela em vez do mvnw:
# o mvnw baixaria o Maven de novo e precisa de programas (curl/unzip) que a imagem pode não ter.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primeiro só o que define as dependências: se o pom.xml não mudou, o Docker reaproveita
# esta camada (cache) e não baixa tudo de novo a cada atualização do código.
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src/ src/
RUN mvn -q -B -DskipTests package && cp target/*.jar app.jar

# ---------- etapa 2: imagem final ----------
FROM eclipse-temurin:21-jre-noble

# Roda com um usuário SEM privilégios (nunca como root dentro do contêiner)
RUN groupadd --system logicup && useradd --system --gid logicup --no-create-home logicup
WORKDIR /app
COPY --from=build --chown=logicup:logicup /app/app.jar app.jar
USER logicup

EXPOSE 8080
# MaxRAMPercentage: o Java usa no máximo 75% da memória dada ao contêiner (mem_limit no compose).
# ExitOnOutOfMemoryError: se a memória acabar, o processo termina e o Docker o reinicia,
# em vez de ficar "meio vivo" e respondendo errado.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
