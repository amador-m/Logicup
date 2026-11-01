# LogiCup — Banco de Dados

Esta pasta é a **fonte da verdade do banco de dados**: a estrutura (schema), o conteúdo pedagógico (seed) e as conferências.

## Arquivos

| Arquivo | O que é | Altera o banco? |
|---|---|---|
| `00_impressao_digital_schema.sql` | Um hash por tabela, que resume colunas, tipos, chaves, FKs e CHECKs. Serve para provar que dois bancos têm a mesma estrutura. | Não (só leitura) |
| `01_schema.sql` | A estrutura completa, criada do zero e sem dados. Serve para o desenvolvimento e para a produção. | Sim (cria as tabelas) |
| `02_seed_conteudo.sql` | As 10 fases, 20 rodadas, 110 questões e 4 dicas. É **gerado** pelo `gerador/gerar_seed.py`, então não edite à mão. | Sim (INSERT) |
| `03_conferencia_seed.sql` | 24 verificações de integridade, cada uma com o valor esperado e o encontrado. Todas precisam dar "OK". | Não (só leitura) |
| `gerador/gerar_seed.py` | Lê o `BancoDeQuestoesLogiCup_v3_0.docx` e gera o `02_seed_conteudo.sql`. | — |
| `gerador/ajustes_seed.py` | O que o documento não diz de forma legível por programa: as respostas aceitas das lacunas, os ícones da F1.Q8 e as saídas de várias linhas. | — |
| `historico/` | As migrações antigas (S6 a S9) e o gerador antigo da Fase 1. Ficam só como registro. | — |

## Recriar o banco do zero (desenvolvimento)

> **Isto apaga TODOS os dados**: usuários, progresso, turmas e conquistas. Faça o backup do passo 1 antes.

1. **Backup.** No Workbench, vá em *Server > Data Export*, marque o schema `Logicup`, escolha *Dump Structure and Data* e *Export to Self-Contained File*. Salve como `backup_AAAA-MM-DD.sql` **fora** do repositório, porque ele tem e-mails e hashes de senha.
2. **Pare o backend** (Spring).
3. Execute `DROP DATABASE Logicup;`.
4. Execute `01_schema.sql` (o arquivo inteiro).
5. Execute `02_seed_conteudo.sql`. A primeira consulta precisa mostrar "OK: banco vazio, pode seguir".
6. Execute `03_conferencia_seed.sql`. As 24 linhas do primeiro resultado precisam estar "OK".
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
