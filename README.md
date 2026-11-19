# 🎮 LogiCup

> Plataforma web gamificada para o aprendizado de programação em Java.

🌐 **Acesse:** [logicup.dev.br](https://logicup.dev.br) · 📸 [@logicup.oficial](https://instagram.com/logicup.oficial)

O **LogiCup** é um projeto de TCC que torna o aprendizado de Java mais acessível, interativo e motivador para estudantes universitários iniciantes em programação. O aluno percorre uma trilha de fases com teoria, exemplos e exercícios, ganha XP, mantém uma ofensiva diária, desbloqueia conquistas e é acompanhado pelo **Gattuccino**, o mascote da plataforma.

---

# 📚 Sobre o projeto

## Trilha de aprendizado

- 🗺️ **10 fases**, divididas em **20 rodadas** e **110 questões**, do raciocínio lógico até orientação a objetos
- 📖 Cada rodada é uma unidade completa: **teoria → exemplos → exercícios**
- 👾 **Boss** ao fim das rodadas; a próxima rodada é liberada com 80% de acertos
- 🔁 "Refazer as que errei", com feedback explicativo e a resposta correta após cada questão

## Tipos de exercício

- Escolha única, resposta curta (lacunas), texto livre, associação e ordenação
- 🧱 **Blocos** (Blockly), com geração de código Java
- 💻 **Escrever código Java**: editor (CodeMirror) e execução real em sandbox (Piston), corrigida por casos de teste de entrada e saída esperada

## Gamificação

- ⭐ XP, nível e ofensiva (*streak*)
- 🏅 Conquistas em bronze, prata e ouro
- 💎 Gemas e loja de avatares
- 🏆 Ranking geral e ranking da turma

## Outras funcionalidades

- 🧪 **Laboratório de blocos**: espaço livre para montar programas com blocos
- 👩‍🏫 **Área do professor**: turmas com código de convite, acompanhamento dos alunos, tarefas atribuídas e **ementa** com a habilidade desenvolvida por cada questão
- 🔐 Cadastro com confirmação de e-mail, recuperação de senha, **entrar com Google** e exclusão de conta
- 📜 Termos de Uso e Política de Privacidade com registro de aceite (LGPD)
- 🐱 Onboarding com o mascote no primeiro acesso

---

# 🛠️ Tecnologias utilizadas

## Front-end
- React 19 + Vite
- React Router
- Styled Components
- Blockly (exercícios em blocos)
- CodeMirror 6 (editor de código Java)

## Back-end
- Java 21
- Spring Boot 4 (Web MVC, Data JPA/Hibernate, Validation, Mail)
- Spring Security: JWT em cookie *httpOnly*, proteção CSRF, validação de origem e limite de tentativas
- OAuth 2.0 (login com Google)
- Maven + JUnit (testes automatizados)

## Banco de dados
- MySQL
- Seed do conteúdo gerado em Python a partir do Banco de Questões (`.docx`)

## Execução de código
- Piston (*self-hosted*), isolado em uma máquina sem acesso à internet

## Infraestrutura
- Google Cloud (Compute Engine)
- Docker e Docker Compose
- Caddy (HTTPS automático e proxy reverso)
- Brevo (envio de e-mails)

## Engenharia de software
- UML (casos de uso, classes, atividades, sequência, componentes e objetos)
- Documentação de requisitos e regras de negócio
- Modelagem MER
- Scrum
- Git/GitHub

---

# 📂 Estrutura do repositório

```bash
Logicup/
├── 1. Artigos e outros/        # Referências e cronograma
├── 2. Códigos/
│   ├── 3. Banco de Dados/      # Schema, seed, migrações e gerador do seed (ver README da pasta)
│   ├── deploy/                 # Scripts de produção no Google Cloud (ver DEPLOY.md)
│   ├── logicup-backend/        # API em Spring Boot
│   └── logicup-frontend/       # Aplicação em React
├── 3. Documentação técnica/    # Modelos e referências de documentação
├── 4. Documentos LogiCup/      # Requisitos, diagramas UML, MER, styleguide e Banco de Questões
├── 5. Entregas/                # Entregas acadêmicas do TCC
├── 6. Imagens/                 # Logos e identidade visual
├── 7. Piston/                  # Configuração do executor de código
├── LICENSE
└── README.md
```

---

# ▶️ Como rodar localmente

**Pré-requisitos:** Java 21, Node.js, MySQL e Python 3 (só para regerar o seed).

1. **Banco de dados:** crie o banco e o conteúdo seguindo o passo a passo de [`2. Códigos/3. Banco de Dados/README.md`](2.%20Códigos/3.%20Banco%20de%20Dados/README.md) (`01_schema.sql` → `02_seed_conteudo.sql` → `03_conferencia_seed.sql`).
2. **Back-end** (porta 8080), no perfil `dev`:
   ```bash
   cd "2. Códigos/logicup-backend"
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   Variáveis obrigatórias: `DB_PASSWORD` e `JWT_SECRET` (texto aleatório com pelo menos 32 caracteres; nunca faça commit dele). `DB_USERNAME` usa `root` por padrão. Com `ADMIN_EMAIL` e `ADMIN_SENHA`, o perfil `dev` cria um usuário ADMIN de teste. O executor de código vem desligado por padrão (`EXECUTOR_HABILITADO=false`) e os e-mails são apenas registrados no log (`EMAIL_MODO=log`).
3. **Front-end** (porta 5173):
   ```bash
   cd "2. Códigos/logicup-frontend"
   npm install
   npm run dev
   ```
   O Vite repassa `/api` para o back-end, então front e back funcionam como um único site (necessário para o cookie de autenticação).
4. **Testes do back-end:** `./mvnw test`

O deploy em produção está documentado em [`2. Códigos/deploy/DEPLOY.md`](2.%20Códigos/deploy/DEPLOY.md).

---

# 👥 Equipe

Projeto desenvolvido como Trabalho de Conclusão de Curso (TCC) por **Davi, Felipe, Luciana, Manuela e Maria**, estudantes da Fatec São Bernardo do Campo.

---

# 🚀 Objetivo

Promover uma experiência de aprendizagem mais dinâmica e engajadora para estudantes de programação, utilizando mecânicas inspiradas em jogos para estimular o desenvolvimento lógico e técnico.

---

# 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
