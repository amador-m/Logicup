#!/bin/bash
# =====================================================================
# LogiCup — grava (ou troca) o login e a chave SMTP da Brevo no .env da VM. (Sprint 10)
#   sudo bash configurar_email.sh
#
# Pergunta as duas informações no terminal (a CHAVE não aparece enquanto você cola) e grava no .env,
# que só o root consegue ler. Assim a chave nunca passa por um editor de texto, pelo histórico do
# terminal, pelo Git ou pelo chat. Rode de novo sempre que gerar uma chave nova na Brevo.
# Depois, o atualizar.sh (ou "sudo docker compose up -d") faz o backend usar os valores novos.
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

read -r -p "Login SMTP da Brevo (ex.: xxxxxx@smtp-brevo.com): " usuario
[[ "$usuario" =~ ^[^@[:space:]]+@[^@[:space:]]+$ ]] || falha "login inválido (ele tem formato de e-mail)"
read -r -s -p "Chave SMTP (cole e aperte Enter; ela não aparece): " chave; echo
[[ ${#chave} -ge 20 && "$chave" != *[[:space:]]* ]] || falha "a chave parece incompleta (cole a chave SMTP inteira)"

env="$PASTA_APP/.env"
tmp="$(mktemp "$PASTA_APP/.env.XXXXXX")"   # mktemp cria o arquivo só com permissão do root
# copia o .env SEM as linhas SMTP antigas e acrescenta as novas (rodar de novo não duplica)
grep -v -E '^SMTP_(USUARIO|CHAVE)=' "$env" > "$tmp" || true
printf 'SMTP_USUARIO=%s\nSMTP_CHAVE=%s\n' "$usuario" "$chave" >> "$tmp"
chmod 600 "$tmp"
mv "$tmp" "$env"
unset chave

ok "login e chave SMTP gravados no .env (somente leitura para o root)"
info "Para valer agora: sudo bash atualizar.sh   (ou, sem código novo: sudo docker compose up -d backend)"
