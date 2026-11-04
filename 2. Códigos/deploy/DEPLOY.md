# LogiCup — Deploy em produção (Google Cloud)

> Versão 03/11/2026. Domínio: **logicup.dev.br**. E-mail do projeto: **logicup.oficial@gmail.com**.
> Siga na ordem. Cada passo diz **onde** o comando roda: no seu PC, no **Cloud Shell** (terminal do site do
> Google Cloud) ou numa **VM** (pelo botão "SSH" do console).

## Visão geral

```
                    internet
                       │  https://logicup.dev.br  (só 80 e 443 abertos)
              ┌────────▼─────────────────────────────┐
  VM          │ Caddy  ── HTTPS automático, site React, cabeçalhos de segurança
  logicup-app │   │ /api                              │
  e2-medium   │ backend (Spring Boot, perfil prod)    │
  4 GB        │   │ rede "dados" (sem internet)       │
              │ MySQL  ── volume + backup diário      │
              └───┬──────────────────────────────────┘
                  │ porta 2000, rede INTERNA do Google (10.10.0.x)
              ┌───▼──────────────────────────────────┐
  VM          │ Piston ── executa o código dos alunos │  sem IP público
  logicup-    │                                       │  sem acesso à internet
  piston 2 GB └───────────────────────────────────────┘
```

**Por que duas VMs:** o código dos alunos roda numa máquina que **não tem banco, senhas nem internet**. Se um dia
alguém escapar da sandbox do Piston, encontra uma VM vazia e isolada (defesa em profundidade).

**Custo (crédito de teste de US$ 300):**

| Item | US$/mês |
|---|---|
| e2-medium | ≈ 39 |
| e2-small | ≈ 19 |
| discos | ≈ 8 |
| IP fixo | ≈ 4 |
| snapshots | ≈ 1 |
| tráfego | ≈ 2 |
| **Total** | **≈ 73 por mês, ≈ US$ 220 em 90 dias** |

Durante o teste não há cobrança no cartão.

---

## 0. Antes de começar (no seu PC)

1. **O GitHub precisa ter a versão que funciona.** A VM baixa o código do GitHub, não do seu IntelliJ. Copie para o
   clone do repositório tudo o que está só no IntelliJ, rode os testes do backend (`mvnw test`), faça o commit e
   dê push na branch `main`.
2. **(Recomendado, grátis) Ensaio local:** veja a seção [Ensaio no seu PC](#ensaio-no-seu-pc-opcional). Ele testa os
   Dockerfiles antes de gastar crédito.

## 1. Google Cloud: conta, projeto e alertas (no navegador)

1. Em <https://console.cloud.google.com>, entre com **logicup.oficial@gmail.com**. Ative o **teste gratuito**
   (pede cartão só para confirmar identidade).
   - **Nunca** clique em "Fazer upgrade" ou "Ativar conta completa".
   - Os 90 dias começam a contar agora.
2. Crie um projeto chamado `logicup` e selecione-o no topo da tela.
3. **Alertas de orçamento.** Vá em *Faturamento → Orçamentos e alertas → Criar orçamento* e crie os dois:

   | Orçamento | Créditos | Valor | Alertas em | Para que serve |
   |---|---|---|---|---|
   | `LogiCup - cobrança real` | deixe marcado (o padrão) | R$ 10 | 10%, 50% e 100% | Avisa se qualquer centavo for cobrado de verdade |
   | `LogiCup - consumo do crédito` | **desmarque** | R$ 1.600 (≈ US$ 300) | 50%, 75% e 90% | Mostra o ritmo de uso do crédito |

   Os alertas chegam no e-mail do projeto.
4. (Opcional) Dê acesso de leitura ao grupo: *IAM → Conceder acesso*, com o e-mail de cada pessoa e o papel **Leitor**.

## 2. Criar a infraestrutura (Cloud Shell)

1. Abra o **Cloud Shell**: botão `>_` no topo do console.
2. No menu ⋮ do Cloud Shell → **Fazer upload**, envie `deploy/gcp/criar_infra.sh` e `deploy/gcp/piston_internet.sh`.
3. Rode:
   ```bash
   gcloud config set project ID-DO-SEU-PROJETO
   bash criar_infra.sh
   ```
4. No fim, ele mostra o **IP PÚBLICO** do site e o **IP INTERNO** do Piston. Anote os dois.

## 3. DNS no Registro.br (no navegador)

1. No painel do domínio `logicup.dev.br`, use o DNS do próprio Registro.br. Em *Editar zona*, crie:

   | Tipo | Nome | Valor |
   |---|---|---|
   | A | (vazio, ou `@`) | IP PÚBLICO |
   | A | `www` | IP PÚBLICO |

2. A propagação leva de minutos a algumas horas. Para conferir no Cloud Shell: `dig +short logicup.dev.br`.

## 4. VM do Piston (SSH na `logicup-piston`)

Abra o console em *Compute Engine → Instâncias de VM* e clique em **SSH** na linha `logicup-piston`. Depois:

1. **Baixe o código.** Esta é a forma para repositório **privado**; se ele for público, pule direto para o `git clone` com a URL https.
   ```bash
   sudo mkdir -p /opt/logicup && sudo chown "$USER" /opt/logicup && cd /opt/logicup
   ssh-keygen -t ed25519 -f ~/.ssh/logicup_deploy -N "" -C "deploy logicup-piston"
   cat ~/.ssh/logicup_deploy.pub
   ```
2. No GitHub, abra o repositório → *Settings → Deploy keys → Add deploy key*:
   - cole a chave;
   - **deixe "Allow write access" desmarcado** (a chave só consegue ler).
3. Clone:
   ```bash
   GIT_SSH_COMMAND="ssh -i ~/.ssh/logicup_deploy" git clone --depth 1 git@github.com:amador-m/Logicup.git
   cd "Logicup/2. Códigos/deploy/piston" && sudo bash instalar_piston.sh
   ```
4. Ele instala Docker, Piston e Java 15, aplica o ajuste de UTF-8 e testa duas coisas: acentos, e se um programa
   de aluno consegue acessar a internet (precisa **não** conseguir).
5. **Isole a VM.** No **Cloud Shell**:
   ```bash
   bash piston_internet.sh desligar
   ```
   A VM perde o IP público e a saída para a internet. O SSH pelo console continua funcionando.

## 5. VM do site (SSH na `logicup-app`)

1. Baixe o código **igual ao passo 4.1 a 4.3**, com uma chave nova chamada `logicup_deploy` nesta VM. Cada VM tem
   a sua chave; no GitHub, cadastre-a como uma segunda deploy key. Depois:
   ```bash
   cd "/opt/logicup/Logicup/2. Códigos/deploy/app"
   sudo bash instalar.sh
   ```
2. Ele pergunta o domínio, o e-mail e o **IP INTERNO do Piston**. Depois:
   - gera as senhas e a chave do JWT;
   - sobe tudo (na primeira vez, de 5 a 10 minutos, por causa do build);
   - liga o backup diário.
3. Crie o seu administrador:
   ```bash
   sudo bash criar_admin.sh
   ```
4. Para usar `git pull` (atualizações) com a deploy key sem digitar nada:
   ```bash
   git -C /opt/logicup/Logicup config core.sshCommand "ssh -i ~/.ssh/logicup_deploy"
   ```

## 6. Conferência final

| O quê | Como | Esperado |
|---|---|---|
| HTTPS | abrir <https://logicup.dev.br> | cadeado, sem aviso |
| Redirecionamentos | <http://logicup.dev.br> e <https://www.logicup.dev.br> | vão para `https://logicup.dev.br` |
| Cabeçalhos de segurança | <https://securityheaders.com> com o domínio | nota A |
| Cadastro e login | criar uma conta de aluno com um e-mail seu | chega o e-mail "Confirme seu e-mail"; o botão leva direto à trilha; depois chega o de boas-vindas |
| Esqueci a senha | login → "Esqueci minha senha" | chega o e-mail; a senha nova funciona no login |
| Executor | uma questão de código e o Laboratório → Executar | saída aparece, com acentos |
| Blocos | F1.Q4 e o Laboratório | blocos aparecem e executam |
| Celular | abrir no celular | layout ok |
| Admin | entrar com `admin` | aceite dos Termos, depois a área de admin |
| Backup | `sudo ls -lh /var/backups/logicup` (VM app) | um `.sql.gz` de hoje |
| Snapshot | console → *Compute Engine → Snapshots* (no dia seguinte) | um snapshot por dia |
| Piston isolado | SSH na `logicup-piston`: `curl -m 5 https://www.google.com` | **falha** (timeout) |

---

## Operação do dia a dia (VM app, na pasta `deploy/app`)

| Tarefa | Comando |
|---|---|
| Ver se está tudo de pé | `sudo docker compose ps` |
| Log do backend ao vivo | `sudo docker compose logs -f --tail=100 backend` |
| Atualizar com o código novo do GitHub | `sudo bash atualizar.sh` (faz backup antes) |
| Backup manual | `sudo bash backup.sh` |
| Restaurar um backup | `sudo bash restaurar.sh /var/backups/logicup/logicup_AAAA-MM-DD_HHMMSS.sql.gz` |
| Reiniciar só o backend | `sudo docker compose restart backend` |
| Gravar/trocar a chave SMTP da Brevo | `sudo bash configurar_email.sh` e depois `sudo docker compose up -d backend` |
| Ver se os e-mails estão saindo | `sudo docker compose logs backend \| grep -i "e-mail"` (o endereço aparece mascarado: `m***@gmail.com`) |

**Backups:**
- **Dump diário**, às 03:15, mantendo 14. Os arquivos `antes-de-restaurar_*` (criados pelo `restaurar.sh`) não são apagados sozinhos.
- **Snapshot do disco**, diário, mantendo 7.
- **Cópia fora do Google** (faça de vez em quando, pelo **Cloud Shell**):
  ```bash
  gcloud compute ssh logicup-app --zone=southamerica-east1-b --tunnel-through-iap \
    --command='sudo cat $(ls -1t /var/backups/logicup/logicup_*.sql.gz | head -1)' > logicup_backup.sql.gz
  ```
  Depois baixe pelo menu ⋮ → *Fazer download*. O arquivo tem dados dos alunos: guarde em lugar seguro.

### Migrações do banco (09_..., 10_...)

O `atualizar.sh` não mexe na estrutura do banco. Quando houver uma migração nova:

1. Faça o backup: `sudo bash backup.sh`.
2. Copie o arquivo para dentro do MySQL e abra o console como root:
   ```bash
   sudo docker compose cp "../../3. Banco de Dados/10_migracao_x.sql" mysql:/tmp/m.sql
   sudo docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4'
   ```
3. Dentro do console:
   - rode `source /tmp/m.sql;`;
   - leia a conferência;
   - se estiver certa, `COMMIT;`, senão `ROLLBACK;`;
   - saia com `exit`.
4. Depois rode `sudo bash atualizar.sh`.

> A migração 10 só tem comandos de estrutura (ALTER/CREATE) e um UPDATE: o MySQL grava cada um na hora,
> então não existe o passo do `COMMIT`/`ROLLBACK`. Para desfazer, use `10_reverter_contas_email_google.sql`.

### Atualização da Sprint 10 (e-mails) — fazer UMA vez, nesta ordem

O backend novo **não sobe** sem a chave da Brevo (de propósito: melhor um erro claro do que cadastros sem e-mail).
Por isso a ordem importa. Tudo na VM `logicup-app`, na pasta `deploy/app`:

1. Backup: `sudo bash backup.sh`.
2. Migração 10, como na seção acima, com o arquivo `10_migracao_contas_email_google.sql`.
   O site antigo continua funcionando com o banco novo (as colunas novas são opcionais para ele).
   Na conferência do final, `usuarios` e `confirmados` têm que ser **iguais**, e `colunas_novas` = **2**.
3. Baixe o código novo **sem sudo** (assim aparece o script novo de e-mail):
   `git -C /opt/logicup/Logicup pull --ff-only`
4. Grave o login e a chave SMTP da Brevo (a chave não aparece enquanto você cola):
   `sudo bash configurar_email.sh`
5. Atualize: `sudo bash atualizar.sh`.
6. Teste: crie uma conta com um e-mail seu (linhas "Cadastro e login" e "Esqueci a senha" da conferência).

### Alertas (Sprint 10) — fazer UMA vez, no navegador

**1. Site fora do ar → e-mail.** No console do Google Cloud, projeto `logicup`:

1. Menu → **Monitoring** → **Uptime checks** → **Create uptime check**.
2. Preencha:
   - *Protocol*: **HTTPS**;
   - *Resource type*: **URL**;
   - *Hostname*: `logicup.dev.br`;
   - *Path*: `/api/auth/csrf`. Esse endereço passa pelo Caddy E pelo backend: se qualquer um dos dois cair, o teste falha;
   - *Check frequency*: **5 minutes**.
3. Em *Response validation*, deixe **2xx** e marque a validação do certificado SSL.
4. Em *Alert & notification*:
   - deixe o alerta **ligado**, com duração de **5 minutos**. Isso evita alarme por uma falha de poucos segundos;
   - em *Notification channels* → **Manage notification channels** → *Email* → `logicup.oficial@gmail.com`;
   - volte e selecione esse canal.
5. Nome: `LogiCup no ar`. Clique em **Test** (deve aparecer *200*/*204* em verde) e depois em **Create**.

**Custo:** a cada 5 minutos, de 3 regiões, dá cerca de 26 mil verificações por mês. O grátis é **1 milhão por mês** por projeto. Alertas só começam a ser cobrados em setembro de 2027.

**Para conferir:** em `sudo docker compose stop backend`, o e-mail chega em uns 5 a 10 minutos. Depois rode `sudo docker compose start backend`; quando o site voltar, chega o e-mail de "resolvido".

**2. Cota da Brevo perto do fim → e-mail.** Não precisa configurar nada: o backend conta os e-mails do dia (`logicup.email.cota.*`).
- Ao chegar em **240 de 300**, manda **um** aviso para `contato@logicup.dev.br` (que o ImprovMX encaminha para o Gmail).
- Em **270**, para as boas-vindas, para sobrar cota para as confirmações.
- Em **300**, para tudo até a meia-noite.
- Para ver no log: `sudo docker compose logs backend | grep COTA_EMAIL`.

### Teste de capacidade (Sprint 10)

Na VM `logicup-app`, pasta `deploy/app`: `python3 teste_capacidade.py`.
- Ele simula uma turma ao mesmo tempo: 40 aberturas do site, 20 logins errados e 30 programas Java no Piston. Depois mostra os tempos.
- Não grava nada no banco. Os 20 logins errados contam no limite de erros por IP da própria VM, que é esquecido em 15 minutos.

### Manutenção da VM do Piston

A VM do Piston fica sem internet, então ela não recebe atualizações sozinha. Uma vez por mês (ou para atualizar o Piston):

1. No **Cloud Shell**: `bash piston_internet.sh ligar`.
2. Na VM do Piston: `sudo apt update && sudo apt upgrade -y && sudo reboot`.
3. No **Cloud Shell**: `bash piston_internet.sh desligar`.

---

## Ensaio no seu PC (opcional)

Ele usa o Docker Desktop, que você já tem por causa do Piston. **Não gasta crédito** e pega problemas de build
antes da VM.

1. Suba o Piston de desenvolvimento normalmente (`7. Piston/piston`).
2. Em `2. Códigos/deploy/app`, copie `.env.exemplo` para `.env` e troque:
   - `DOMINIO=localhost`;
   - as três senhas por qualquer texto de **24 ou mais letras e números**;
   - `EXECUTOR_URL=http://logicup_piston:2000`.
   **Salve o `.env` (Ctrl+S) antes de continuar.** O MySQL lê as senhas só na primeira subida; se subir
   com as senhas de exemplo, o usuário do backend não é criado e o MySQL fica `unhealthy`. Nesse caso,
   corrija o `.env` e recomece com `docker compose down -v`.
3. No PowerShell, na pasta `deploy/app`:
   ```powershell
   docker compose up -d --build
   docker network connect logicup_web logicup_piston
   docker compose ps
   ```
4. Abra <https://localhost>. O navegador avisa sobre o certificado: no ensaio, o Caddy usa um certificado local.
   Clique em *Avançado → Continuar*.
5. Para desligar e apagar tudo do ensaio: `docker compose down -v`. O `-v` apaga o banco **do ensaio**.

**Diferenças do ensaio:**
- No Windows, o MySQL ignora o `logicup.cnf` (aparece "World-writable config file is ignored" no log). Na VM Linux isso não acontece.
- As portas 80 e 443 do seu PC precisam estar livres.

---

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| Site sem HTTPS / aviso de certificado | DNS ainda não aponta para a VM | Conferir o passo 3. O Caddy tenta de novo sozinho; depois: `sudo docker compose restart caddy` |
| Questões de código: "execução indisponível" | Piston parado, ou IP errado no `.env` | Na VM do Piston: `sudo docker compose ps`. Na VM app: `curl http://IP-INTERNO:2000/api/v2/runtimes` |
| Backend reiniciando | Erro na subida | `sudo docker compose logs backend \| tail -50` |
| Muitos "Muitas tentativas" numa aula | Limite de cadastro: 10 contas por hora por IP; uma sala inteira sai pelo mesmo IP da faculdade | Cadastrar os alunos antes da aula, ou ajustar `logicup.limites.cadastro.maximo` |

## Fim do período de teste (janeiro/2027)

- Até **meados de janeiro**, decidir se o LogiCup continua.
- Se continuar, migrar até o **dia ~75** do teste: o plano está no `claude/hospedagem-comparativo.md`, e estes mesmos scripts funcionam em qualquer VPS Ubuntu.
- Se não continuar, baixe o último backup e deixe o teste acabar: **não há cobrança**.

## Para a banca: o que explicar

- **Docker e Compose:** cada parte roda isolada num contêiner, descrito em arquivo. O ambiente é reproduzível, e
  migrar de provedor significa rodar os mesmos scripts.
- **Proxy reverso (Caddy):** um único endereço público e HTTPS automático com Let's Encrypt. Front e API ficam na
  mesma origem, por isso o cookie de login `__Host-` com `SameSite=Strict` funciona sem CORS.
- **Defesa em profundidade:**
  - firewall (só 80 e 443);
  - banco numa rede sem internet;
  - usuário do banco com o mínimo de permissões;
  - executor numa VM isolada e sem internet;
  - cabeçalhos de segurança (HSTS, CSP);
  - SSH só pelo IAP do Google.
- **IP real atrás do proxy:** o limite de tentativas de login é por IP. O backend só aceita o `X-Forwarded-For`
  vindo do Caddy; sem isso, todos os alunos dividiriam um único limite.
- **Segredos:** nenhum segredo no GitHub. O `.env` é gerado na VM com valores aleatórios.
- **Backup em duas camadas:** `mysqldump` diário, com restauração testada, mais o snapshot diário do disco.
