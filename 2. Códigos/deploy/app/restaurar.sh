#!/bin/bash
# =====================================================================
# LogiCup — RESTAURA o banco a partir de um backup do backup.sh.
#   sudo bash restaurar.sh /var/backups/logicup/logicup_2026-11-20_031500.sql.gz
#
# ATENÇÃO: substitui TODO o banco atual pelo conteúdo do backup (o que foi feito depois dele se perde).
# Por segurança, antes de restaurar ele faz um backup do estado ATUAL (para dar para voltar).
# O backend fica parado durante a restauração, para ninguém gravar no meio.
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

arquivo="${1:-}"
[[ -n "$arquivo" ]] || { ls -1t "$PASTA_BACKUPS"/logicup_*.sql.gz 2>/dev/null | head -5; falha "diga qual backup restaurar (acima, os 5 mais recentes)"; }
[[ -f "$arquivo" ]] || falha "arquivo não encontrado: $arquivo"
gzip -t "$arquivo" || falha "o arquivo está corrompido"
zcat "$arquivo" | tail -n 1 | grep -q "Dump completed" || falha "o backup está incompleto"

aviso "O banco ATUAL será substituído por: $arquivo"
if [[ "${CONFIRMAR:-}" != "RESTAURAR" ]]; then
    read -r -p "Digite RESTAURAR para continuar: " resposta
    [[ "$resposta" == "RESTAURAR" ]] || falha "cancelado"
fi

# trabalha numa CÓPIA do backup escolhido: nada do que acontece daqui em diante pode alterá-lo
copia="$(mktemp --suffix=.sql.gz)"
trap 'rm -f "$copia"' EXIT
cp -- "$arquivo" "$copia"

info "1/4 Backup de segurança do estado atual (antes-de-restaurar_...; não entra na rotação dos 14)"
PREFIXO=antes-de-restaurar bash "$PASTA_APP/backup.sh"

info "2/4 Parando o backend (o site mostra erro na API durante alguns segundos)"
compose stop backend

info "3/4 Restaurando"
# o backup tem "CREATE DATABASE ... / USE Logicup": apagamos o banco antes para não sobrar tabela antiga.
# O usuário logicup_app e as permissões dele ficam no banco "mysql", que não é tocado.
compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot -e "DROP DATABASE IF EXISTS Logicup"'
zcat "$copia" | compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4'

info "4/4 Subindo o backend"
compose start backend
espera_saudavel backend 180 || falha "o backend não voltou; veja: sudo docker compose logs backend"
ok "banco restaurado a partir de $(basename "$arquivo")"
