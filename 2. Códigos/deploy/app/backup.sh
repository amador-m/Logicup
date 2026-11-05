#!/bin/bash
# =====================================================================
# LogiCup — BACKUP do banco (mysqldump). Roda sozinho todo dia às 03:15 (timer do systemd criado
# pelo instalar.sh) e também antes de cada atualização (atualizar.sh). Pode rodar à mão:
#   sudo bash backup.sh
#
# Gera /var/backups/logicup/logicup_AAAA-MM-DD_HHMMSS.sql.gz e mantém os 14 mais recentes.
# Com PREFIXO=xxx, o nome fica xxx_AAAA-...; o restaurar.sh usa "antes-de-restaurar" no backup de segurança.
# --single-transaction: copia um "retrato" consistente do banco SEM travar o site (InnoDB).
# É a 1ª camada de backup; a 2ª é o snapshot diário do disco no Google Cloud (ver DEPLOY.md).
# Os arquivos contêm dados pessoais dos alunos: ficam só para o root (chmod 600).
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

MANTER="${MANTER:-14}"
install -d -m 700 "$PASTA_BACKUPS"
arquivo="$PASTA_BACKUPS/${PREFIXO:-logicup}_$(date +%F_%H%M%S).sql.gz"
[[ ! -e "$arquivo" ]] || falha "já existe $arquivo (dois backups no mesmo segundo?)"
temporario="$arquivo.parcial"
trap 'rm -f "$temporario"' EXIT

info "Gerando backup em $arquivo"
# MYSQL_PWD dentro do contêiner: a senha não aparece na linha de comando (nem no "ps")
compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot \
        --single-transaction --routines --triggers --events --set-gtid-purged=OFF \
        --default-character-set=utf8mb4 --databases Logicup' \
    | gzip -9 > "$temporario"
chmod 600 "$temporario"

# Conferências: o gzip está íntegro e o mysqldump chegou ao fim (ele escreve "Dump completed")
gzip -t "$temporario" || falha "o arquivo de backup está corrompido"
zcat "$temporario" | tail -n 1 | grep -q "Dump completed" || falha "o mysqldump não terminou (backup incompleto)"
# a lista das tabelas é lida UMA vez (grep lê o arquivo inteiro; um "grep -q" direto no zcat pararia no meio e,
# com o pipefail ligado, o zcat interrompido faria a conferência falhar mesmo com o backup certo)
lista="$(zcat "$temporario" | grep '^CREATE TABLE' || true)"
tabelas="$(grep -c . <<< "$lista" || true)"
# Sprint 10: antes era "15 ou mais", um número fixo que quebrou quando a migração 11 apagou 2 tabelas sem uso.
# Agora a conferência não depende do número exato: exige as tabelas que guardam os dados dos alunos
# (se elas faltarem, o backup não serve) e um mínimo folgado contra um dump vazio pela metade.
(( tabelas >= 10 )) || falha "o backup tem só $tabelas tabelas (esperado: 10 ou mais)"
for essencial in usuario aluno progresso_exercicio exercicio aceite_documento; do
    grep -q "^CREATE TABLE \`$essencial\`" <<< "$lista" || falha "o backup não tem a tabela $essencial"
done

mv "$temporario" "$arquivo"
ok "backup pronto: $(du -h "$arquivo" | cut -f1), $tabelas tabelas"

# Rotação: apaga os mais antigos, mantendo os $MANTER mais recentes
mapfile -t antigos < <(ls -1t "$PASTA_BACKUPS"/logicup_*.sql.gz 2>/dev/null | tail -n +"$((MANTER + 1))")
if (( ${#antigos[@]} > 0 )); then
    rm -f -- "${antigos[@]}"
    info "${#antigos[@]} backup(s) antigo(s) removido(s); ficam os $MANTER mais recentes"
fi
