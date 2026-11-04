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

info "5/5 Conferindo"
espera_saudavel backend 240 || falha "o backend não ficou saudável (veja: sudo docker compose logs backend). Para voltar à versão anterior: git reset --hard $antes (sem sudo) e depois sudo docker compose up -d --build"
compose ps
docker image prune -f >/dev/null   # apaga as imagens antigas (sem uso) para não encher o disco
ok "LogiCup atualizado para $depois"
