#!/bin/bash
# =====================================================================
# LogiCup — cria o usuário do banco usado pelo BACKEND (roda UMA vez, na primeira subida do MySQL,
# depois do 01_schema.sql e do 02_seed_conteudo.sql).
#
# Princípio do menor privilégio: o backend só precisa LER e GRAVAR dados.
#   - SELECT, INSERT, UPDATE, DELETE no banco Logicup;
#   - SEM CREATE/DROP/ALTER/GRANT: mesmo que alguém explore uma falha no backend, não consegue
#     apagar tabelas, mudar a estrutura nem criar usuários.
#   - só a partir da rede "dados" do docker-compose (172.29.0.x).
# Migrações (ALTER TABLE etc.) continuam sendo feitas por você, com o root, pela seção "Migrações" do DEPLOY.md.
# =====================================================================
# Sem "set -u": dependendo das permissões, o entrypoint do MySQL "inclui" este arquivo (source) em vez
# de executá-lo, e o set -u passaria a valer para o resto do entrypoint. Por isso as checagens são explícitas.
if [[ ! "${DB_APP_PASSWORD:-}" =~ ^[A-Za-z0-9]{24,}$ ]]; then
    echo "03_usuario_app.sh: DB_APP_PASSWORD ausente ou fraca (gere com o instalar.sh)" >&2
    exit 1
fi

if mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE USER 'logicup_app'@'172.29.0.%' IDENTIFIED BY '${DB_APP_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE ON Logicup.* TO 'logicup_app'@'172.29.0.%';
SQL
then
    echo "03_usuario_app.sh: usuário logicup_app criado"
else
    echo "03_usuario_app.sh: FALHOU ao criar o usuário logicup_app" >&2
    exit 1
fi
