#!/bin/bash
# =====================================================================
# LogiCup — funções usadas pelos outros scripts de deploy/app (não rode este arquivo sozinho).
# =====================================================================

# pasta deste script (deploy/app), seja qual for a pasta de onde ele foi chamado
PASTA_APP="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# backups ficam FORA do repositório (um "git clean" nunca os apaga)
PASTA_BACKUPS="${PASTA_BACKUPS:-/var/backups/logicup}"

info()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
ok()    { printf '\033[1;32mOK\033[0m  %s\n' "$*"; }
aviso() { printf '\033[1;33mATENÇÃO\033[0m %s\n' "$*" >&2; }
falha() { printf '\033[1;31mERRO\033[0m %s\n' "$*" >&2; exit 1; }

precisa_root() {
    [[ $EUID -eq 0 ]] || falha "rode com sudo: sudo $0"
}

# docker compose sempre a partir da pasta deploy/app (é lá que estão o compose e o .env)
compose() {
    docker compose --project-directory "$PASTA_APP" -f "$PASTA_APP/docker-compose.yml" "$@"
}

precisa_env() {
    [[ -f "$PASTA_APP/.env" ]] || falha "não achei $PASTA_APP/.env. Rode antes: sudo bash instalar.sh"
}

# espera um serviço do compose ficar "healthy" (máx. $2 segundos)
espera_saudavel() {
    local servico="$1" limite="${2:-180}" passou=0 estado id
    while (( passou < limite )); do
        id="$(compose ps -q "$servico" 2>/dev/null)"
        estado="$( [[ -n "$id" ]] && docker inspect -f '{{.State.Health.Status}}' "$id" 2>/dev/null )"
        [[ "$estado" == "healthy" ]] && return 0
        sleep 5; passou=$((passou + 5))
    done
    return 1
}
