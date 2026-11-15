#!/bin/bash
# =====================================================================
# LogiCup — grava (ou troca) o Client ID e a chave secreta do "Entrar com Google" no .env da VM. (Sprint 10)
#   sudo bash configurar_google.sh
#
# Onde pegar: Google Cloud > Google Auth Platform > Clientes > LogiCup Web.
# A CHAVE não aparece enquanto você cola (não fica no histórico do terminal nem em print).
# Depois: sudo docker compose up -d backend   (ou o atualizar.sh, se houver código novo)
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

read -r -p "Client ID (termina em .apps.googleusercontent.com): " id
[[ "$id" =~ ^[0-9]+-[a-z0-9]+\.apps\.googleusercontent\.com$ ]] || falha "Client ID com formato inesperado"
read -r -s -p "Chave secreta do cliente (cole e aperte Enter; ela não aparece): " chave; echo
[[ "$chave" =~ ^GOCSPX-[A-Za-z0-9_-]{20,}$ ]] || falha "a chave parece incompleta (começa com GOCSPX-)"

env="$PASTA_APP/.env"
tmp="$(mktemp "$PASTA_APP/.env.XXXXXX")"
grep -v -E '^GOOGLE_CLIENT_(ID|SECRET)=' "$env" > "$tmp" || true
printf 'GOOGLE_CLIENT_ID=%s\nGOOGLE_CLIENT_SECRET=%s\n' "$id" "$chave" >> "$tmp"
chmod 600 "$tmp"
mv "$tmp" "$env"
unset chave

ok "Client ID e chave do Google gravados no .env (somente leitura para o root)"
info "Para valer agora: sudo docker compose up -d backend"
