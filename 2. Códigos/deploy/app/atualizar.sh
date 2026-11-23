#!/bin/bash
# =====================================================================
# LogiCup — ATUALIZA o site com a versão mais nova do GitHub.
#   sudo bash atualizar.sh
#
# 1) backup do banco  2) baixa o código (git pull)  3) reconstrói as imagens  4) troca os contêineres
# 5) confere se voltou. O site fica fora do ar só alguns segundos (troca do backend).
#
# ATENÇÃO: este script NÃO roda migrações do banco. Se a atualização tiver uma migração (09_..., 10_...),
# siga a seção "Migrações" do DEPLOY.md ANTES de rodar este script.
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

# Sprint 10: sem a chave da Brevo o backend novo não sobe; melhor parar AQUI, antes de mexer em algo
grep -q '^SMTP_CHAVE=.\+' "$PASTA_APP/.env" \
    || falha "faltam o login e a chave SMTP da Brevo no .env. Rode antes: sudo bash configurar_email.sh"

info "1/5 Backup antes de atualizar"
bash "$PASTA_APP/backup.sh"

info "2/5 Baixando o código"
# o git roda como o DONO do clone (não como root): é dele a deploy key, e o git recusa repositórios
# de outro usuário ("dubious ownership")
dono="$(stat -c %U "$PASTA_APP")"
git_() { sudo -u "$dono" git -C "$PASTA_APP" "$@"; }
antes="$(git_ rev-parse --short HEAD)"
git_ pull --ff-only
depois="$(git_ rev-parse --short HEAD)"
info "versão $antes -> $depois"

info "3/5 Construindo as imagens"
compose build --pull

info "4/5 Trocando os contêineres"
compose up -d
# Sprint 10: o Caddyfile entra por VOLUME. Se só ele mudou (e a imagem do site não), o "up -d" não recria o
# contêiner do Caddy e a configuração nova não valeria. O reload aplica sem derrubar nada e, se o arquivo
# tiver erro, o Caddy recusa e continua com a configuração antiga.
#
# Sprint 11: quando o "up -d" RECRIA o contêiner do Caddy, a porta de administração dele (que o reload usa)
# leva alguns segundos para abrir. Antes, o reload rodava cedo demais e mostrava um aviso falso de erro.
# Agora tenta até 10 vezes, com 2 s entre elas. Se o Caddy RECUSAR o arquivo (erro de verdade no Caddyfile),
# o aviso aparece igual: o "adapt" confere o arquivo antes, então distinguimos "ainda subindo" de "arquivo errado".
if ! compose exec -T caddy caddy adapt --config /etc/caddy/Caddyfile --adapter caddyfile >/dev/null 2>&1; then
    aviso "o Caddyfile novo tem erro e NÃO foi aplicado; o Caddy continua com o anterior (veja: sudo docker compose exec caddy caddy adapt --config /etc/caddy/Caddyfile)"
else
    recarregou=0
    for _ in $(seq 1 10); do
        if compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile >/dev/null 2>&1; then
            recarregou=1
            break
        fi
        sleep 2
    done
    [ "$recarregou" = 1 ] \
        || aviso "o Caddy não respondeu ao reload em 20 s (veja: sudo docker compose logs caddy)"
fi

info "5/5 Conferindo"
espera_saudavel backend 240 || falha "o backend não ficou saudável (veja: sudo docker compose logs backend). Para voltar à versão anterior: git reset --hard $antes (sem sudo) e depois sudo docker compose up -d --build"
compose ps
docker image prune -f >/dev/null   # apaga as imagens antigas (sem uso) para não encher o disco
ok "LogiCup atualizado para $depois"
