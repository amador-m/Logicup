# LogiCup — Banco de Dados

Esta pasta é a **fonte da verdade do banco de dados**: a estrutura (schema), o conteúdo pedagógico (seed) e as conferências.

## Arquivos

| Arquivo | O que é | Altera o banco? |
|---|---|---|
| `00_impressao_digital_schema.sql` | Um hash por tabela, que resume colunas, tipos, chaves, FKs e CHECKs. Serve para provar que dois bancos têm a mesma estrutura. | Não (só leitura) |
| `01_schema.sql` | A estrutura completa, criada do zero e sem dados. Serve para o desenvolvimento e para a produção. | Sim (cria as tabelas) |
| `02_seed_conteudo.sql` | As 10 fases, 20 rodadas, 110 questões e 4 dicas. É **gerado** pelo `gerador/gerar_seed.py`, então não edite à mão. | Sim (INSERT) |
| `03_conferencia_seed.sql` | 26 verificações de integridade, cada uma com o valor esperado e o encontrado. Todas precisam dar "OK". | Não (só leitura) |
| `gerador/gerar_seed.py` | Lê o `BancoDeQuestoesLogiCup_v3_0.docx` e gera o `02_seed_conteudo.sql`. | — |
| `07a_migracao_formato_codigo.sql` | **Sprint 9 (ADR-001).** Acrescenta o formato `CODIGO` ao ENUM. Não altera nenhuma questão. | Sim (só a estrutura) |
| `07b_migracao_questoes_codigo.sql` | **Sprint 9.** As 9 questões "Escrever código" passam a ser corrigidas executando o código. Faz backup antes (`exercicio_backup_s9`). **Aplicar só junto com o editor de código no frontend.** É **gerado** por `gerador/codigo/gerar_migracao_07b.py`. | Sim (9 questões) |
| `07b_reverter_questoes_codigo.sql` | Desfaz a 07b a partir do backup (as 9 questões voltam exatamente como eram). | Sim (9 questões) |
| `08_migracao_questao_blocos.sql` | **Sprint 9 (Blockly).** A F5.Q8 deixa de ser ordenação de linhas e passa a ser montada com **blocos** (formato `BLOCOS`, que já existia no ENUM). Faz backup antes (`exercicio_backup_s9_blocos`). Não mexe no progresso. **Aplicar só junto com o frontend do Blockly.** É **gerado** por `gerador/codigo/gerar_migracao_08.py`. | Sim (1 questão) |
| `08_reverter_questao_blocos.sql` | Desfaz a 08 a partir do backup (a F5.Q8 volta exatamente como era). | Sim (1 questão) |
| `09_migracao_mais_blocos.sql` | **03/11.** Mais 5 questões de ordenação viram **blocos**: F1.Q4 (passos do café), F2.Q4 (programa completo, mais difícil, com blocos que não devem ser usados), F4.Q5, F9.R2.Q8 e F10.Q5. Muda tipo, formato, gabarito, alternativas e o enunciado ("encaixe os blocos"); XP e progresso não mudam. Backup em `exercicio_backup_0311_blocos`. **Aplicar só junto com o frontend dos blocos de linha e o backend com o modo PROGRAMA.** Gerado por `gerador/codigo/gerar_migracao_09.py`. | Sim (5 questões) |
| `09_reverter_mais_blocos.sql` | Desfaz a 09 a partir do backup (as 5 voltam exatamente como eram; a F5.Q8 continua em blocos). | Sim (5 questões) |
| `10_migracao_contas_email_google.sql` | **Sprint 10 (04/11).** Confirmação de e-mail, recuperação de senha e preparação do login com Google: `usuario` ganha `email_confirmado_em` e `google_id` (única), `senha` passa a aceitar NULL (conta só com Google) e nasce a tabela `token_usuario` (hash dos códigos dos links). Contas existentes ficam **confirmadas**. É DDL (sem ROLLBACK). **Aplicar junto com o backend da Sprint 10.** | Sim (estrutura + marca contas como confirmadas) |
| `10_reverter_contas_email_google.sql` | Desfaz a 10. Falha de propósito se houver conta sem senha (criada só pelo Google). | Sim (estrutura) |
| `gerador/codigo/` | Programas de teste das 9 questões de código (`questoes_codigo.py`), o gerador/validador (`gerar_questoes_codigo.py`), a conversão usada pelo seed e pela 07b (`aplicar_codigo.py`) e o gerador da 07b. | — |
| `gerador/ajustes_seed.py` | O que o documento não diz de forma legível por programa: as respostas aceitas das lacunas, os ícones da F1.Q8 e as saídas de várias linhas. | — |
| `historico/` | As migrações antigas (S6 a S9) e o gerador antigo da Fase 1. Ficam só como registro. | — |
| `dev/04_dev_liberar_fases.sql` | **Só em desenvolvimento.** Libera para um aluno de teste uma rodada escolhida, concluindo todas as anteriores. | Sim (só o aluno escolhido) |
| `dev/05_dev_zerar_aluno.sql` | **Só em desenvolvimento.** Volta um aluno de teste ao estado de recém-cadastrado. | Sim (só o aluno escolhido) |

## Recriar o banco do zero (desenvolvimento)

> **Isto apaga TODOS os dados**: usuários, progresso, turmas e conquistas. Faça o backup do passo 1 antes.

1. **Backup.** No Workbench, vá em *Server > Data Export*, marque o schema `Logicup`, escolha *Dump Structure and Data* e *Export to Self-Contained File*. Salve como `backup_AAAA-MM-DD.sql` **fora** do repositório, porque ele tem e-mails e hashes de senha.
2. **Pare o backend** (Spring).
3. Execute `DROP DATABASE Logicup;`.
4. Execute `01_schema.sql` (o arquivo inteiro).
5. Execute `02_seed_conteudo.sql`. A primeira consulta precisa mostrar "OK: banco vazio, pode seguir".
6. Execute `03_conferencia_seed.sql`. As 26 linhas do primeiro resultado precisam estar "OK".
7. Suba o backend no perfil `dev`. O `DatabaseTestRunner` cria o ADMIN a partir de `ADMIN_EMAIL` e `ADMIN_SENHA`.
8. Rode os testes: `mvnw test` na pasta do backend.

**Para desfazer**, restaure o backup do passo 1 (*Server > Data Import*).

## Mudou o Banco de Questões?

```
cd gerador
pip install python-docx
python gerar_seed.py "../../../4. Documentos LogiCup/BancoDeQuestoesLogiCup_v3_0.docx"
```

O gerador **para com uma mensagem de erro** se alguma questão não estiver no formato esperado. Nesse caso, nenhum `.sql` é gravado. Ele confere:
- o XP por código de questão;
- 205 XP por fase (410 na F9 e 350 na F10);
- o Boss como última questão;
- a ordem das questões declarada em cada rodada;
- habilidades com até 255 caracteres;
- a regra do Gattuccino;
- se o gabarito aponta para alternativas que existem.

Em um banco que **já tem alunos**, não rode o seed de novo: o conteúdo precisa ser alterado por uma migração com UPDATE, mantendo os `id_exercicio` (como a migração da S7).

## Decisões de implementação do seed (Sprint 9)

- **Alternativas embaralhadas nas Fases 4 a 10.** No documento, a resposta certa costuma ser a 1ª opção, e o frontend mostra as alternativas na ordem do banco. O embaralhamento usa o código da questão como semente, então rodar o gerador de novo produz o mesmo SQL. As Fases 1 a 3 mantêm a ordem do documento.
- **Teoria com `blocos`.** Cada seção de `conteudo_teorico.teoria` tem `texto`, que a tela atual já mostra, e `blocos` (parágrafo, lista, código, tabela), que a tela nova vai usar para mostrar o código em fonte monoespaçada.
- **Objetivo geral da fase** (`modulo.descricao`) vem da "Finalidade pedagógica" da ementa.
- **Saídas de várias linhas:** o " / " das opções vira quebra de linha (ver `ajustes_seed.py`).
- **F3.Q3 (duas lacunas):** `alternativas_json = {"lacunas": 2}` (público) e `gabarito_json = {"lacunas": [{"aceitas": ["int"]}, {"aceitas": ["18"]}], ...}` (secreto).

## Questões de código (Sprint 9, ADR-001)

As questões F4.Q9, F5.Q9, F6.Q9, F7.Q9, F8.Q9, F9.R1.Q9, F9.R2.Q9, F10.Q8 e F10.Q9 são corrigidas **executando o código do aluno** no Piston. O código do aluno é encaixado num *programa de teste* (o marcador `//@CODIGO_DO_ALUNO`), que roda todos os casos de teste ocultos numa única execução.

Para mudar uma dessas questões (casos, programa de teste, exemplos):

1. Edite `gerador/codigo/questoes_codigo.py`.
2. Rode `python3 gerador/codigo/gerar_questoes_codigo.py`. Ele executa a solução de referência com Java (`--source 15`, a versão do Piston), **gera as saídas esperadas** e **valida**: as soluções certas precisam passar e os erros típicos precisam reprovar. Se algo falhar, nada é gerado.
3. Rode o `gerar_seed.py` (banco novo) e/ou `python3 gerador/codigo/gerar_migracao_07b.py` (banco existente).
4. Copie `gerador/codigo/questoes-codigo-teste.json` para `src/test/resources/questoes/` do backend e rode o `QuestoesCodigoRealTest` com o Piston ligado.

O gabarito continua com as palavras-chave: elas são a **reserva** quando o executor está fora do ar. Nesse caso, a questão fica *pendente*, sem XP e sem contar como erro.

## Questões de blocos (Sprint 9, etapa 1 do Blockly)

A F5.Q8 é montada com **blocos** (Blockly). Os blocos viram Java no navegador e a correção é a mesma das
questões de código: o Java gerado roda no programa de teste. Os blocos iniciais (soltos e embaralhados, sem
poder apagar) ficam em `alternativas_json.blocosIniciais`; o gabarito tem o mesmo formato das questões de código.

Para mudar a questão: edite `gerador/codigo/questoes_blocos.py` e siga os mesmos passos das questões de código
(o `gerar_questoes_codigo.py` valida as duas), usando `gerar_migracao_08.py` para o banco existente.
A `referencia` precisa ser **exatamente** o Java que o gerador de blocos produz com a montagem certa.

## Testar uma fase sem jogar as anteriores (só em desenvolvimento)

1. Cadastre um aluno de teste pelo site, por exemplo com o username `aluna.teste`.
2. Abra `dev/04_dev_liberar_fases.sql` e ajuste `@username`, `@fase_alvo` e `@rodada_alvo`. Para concluir a trilha inteira, use `@fase_alvo = 11`.
3. Execute o arquivo inteiro e confira a última aba: tudo antes do alvo deve estar APROVADA, o alvo LIBERADA e o resto BLOQUEADA.
4. Se estiver certo, execute `COMMIT;` e recarregue a trilha no site (F5).
5. Para repetir o teste do zero, use `dev/05_dev_zerar_aluno.sql`.

O script **nunca** altera questões que o aluno já concluiu de verdade. Ele ignora professores e usernames inexistentes. As conquistas não são criadas pelo script: o backend as concede de forma retroativa no próximo envio de rodada do aluno.

**Nunca rode os scripts de `dev/` no banco de produção.**
