#!/bin/bash
# =====================================================================
# LogiCup — PREPARA a VM "logicup-app" do zero e sobe o site. Rode UMA vez, na VM nova:
#   sudo bash instalar.sh
# (Pode rodar de novo sem estragar nada: cada etapa confere se já foi feita.)
#
# O que ele faz:
#  1. fuso horário de Brasília e 2 GB de swap (folga de memória para picos)
#  2. instala Docker e Docker Compose pelos pacotes do Ubuntu (que recebem as atualizações de
#     segurança automáticas do Ubuntu)
#  3. cria o .env com senhas e chave do JWT ALEATÓRIAS (ninguém precisa digitar nem saber)
#  4. confere se o domínio já aponta para esta VM (senão o HTTPS não funciona)
#  5. sobe o LogiCup (na 1ª vez o MySQL carrega o schema e o seed: leva 1 a 2 minutos)
#  6. liga o backup diário (03:15) pelo systemd
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root

# ---------------------------------------------------------------- 1. sistema
info "1/6 Fuso horário e swap"
timedatectl set-timezone America/Sao_Paulo
if ! swapon --show | grep -q /swapfile; then
    fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile >/dev/null && swapon /swapfile
    grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
    echo 'vm.swappiness=10' > /etc/sysctl.d/90-logicup.conf && sysctl -q -p /etc/sysctl.d/90-logicup.conf
fi
ok "fuso $(timedatectl show -p Timezone --value), swap $(swapon --show --noheadings | awk '{print $3}')"

# ---------------------------------------------------------------- 2. Docker
info "2/6 Docker"
if ! command -v docker >/dev/null || ! docker compose version >/dev/null 2>&1; then
    apt-get update -q
    DEBIAN_FRONTEND=noninteractive apt-get install -y -q docker.io docker-compose-v2 git openssl dnsutils
fi
# log dos contêineres com tamanho máximo (o padrão do Docker cresce sem limite e enche o disco)
if [[ ! -f /etc/docker/daemon.json ]]; then
    echo '{ "log-driver": "json-file", "log-opts": { "max-size": "10m", "max-file": "5" } }' > /etc/docker/daemon.json
    systemctl restart docker
fi
systemctl enable --now docker >/dev/null
ok "$(docker --version), $(docker compose version --short)"

# ---------------------------------------------------------------- 3. .env
info "3/6 Configuração (.env)"
if [[ -f "$PASTA_APP/.env" ]]; then
    ok ".env já existe: mantido (as senhas NÃO são trocadas)"
else
    read -r -p "Domínio [logicup.dev.br]: " dominio;              dominio="${dominio:-logicup.dev.br}"
    read -r -p "E-mail para o certificado HTTPS [logicup.oficial@gmail.com]: " email
    email="${email:-logicup.oficial@gmail.com}"
    read -r -p "IP INTERNO da VM do Piston (ex.: 10.10.0.3): " ip_piston
    [[ "$ip_piston" =~ ^10\.[0-9]+\.[0-9]+\.[0-9]+$ ]] || falha "IP interno inválido (deve começar com 10.)"
    umask 077   # o arquivo nasce legível só pelo root
    cat > "$PASTA_APP/.env" <<EOF
# Gerado pelo instalar.sh em $(date '+%d/%m/%Y %H:%M'). NÃO copie este arquivo para lugar nenhum.
DOMINIO=$dominio
ACME_EMAIL=$email
MYSQL_ROOT_PASSWORD=$(openssl rand -hex 24)
DB_APP_PASSWORD=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 48)
EXECUTOR_URL=http://$ip_piston:2000
EXECUTOR_HABILITADO=true
EOF
    umask 022
    ok ".env criado (somente leitura para o root)"
fi
dominio="$(grep '^DOMINIO=' "$PASTA_APP/.env" | cut -d= -f2)"
executor="$(grep '^EXECUTOR_URL=' "$PASTA_APP/.env" | cut -d= -f2)"

# ---------------------------------------------------------------- 4. DNS e Piston
info "4/6 Conferindo o domínio e o Piston"
ip_publico="$(curl -sf -H 'Metadata-Flavor: Google' \
    http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip || true)"
ip_dns="$(dig +short A "$dominio" @8.8.8.8 | tail -n1)"
if [[ -n "$ip_publico" && "$ip_dns" == "$ip_publico" ]]; then
    ok "$dominio aponta para esta VM ($ip_publico)"
else
    aviso "$dominio aponta para '${ip_dns:-nada}', mas o IP desta VM é '${ip_publico:-desconhecido}'."
    aviso "O site sobe, mas o HTTPS só funciona depois que o DNS estiver certo (o Caddy tenta de novo sozinho)."
fi
if curl -sf --max-time 5 "$executor/api/v2/runtimes" | grep -q '"java"'; then
    ok "Piston respondendo em $executor (com Java)"
else
    aviso "o Piston não respondeu em $executor. O site funciona, mas as questões de código ficam sem execução."
fi

# ---------------------------------------------------------------- 5. subir
info "5/6 Construindo e subindo o LogiCup (a 1ª vez leva alguns minutos)"
compose up -d --build
espera_saudavel mysql 300 || falha "o MySQL não ficou pronto; veja: sudo docker compose logs mysql"
espera_saudavel backend 300 || falha "o backend não ficou pronto; veja: sudo docker compose logs backend"
compose ps

# ---------------------------------------------------------------- 6. backup diário
info "6/6 Backup diário"
cat > /etc/systemd/system/logicup-backup.service <<EOF
[Unit]
Description=LogiCup - backup diario do banco
After=docker.service

[Service]
Type=oneshot
ExecStart=/bin/bash "$PASTA_APP/backup.sh"
EOF
cat > /etc/systemd/system/logicup-backup.timer <<'EOF'
[Unit]
Description=LogiCup - backup diario as 03:15

[Timer]
OnCalendar=*-*-* 03:15:00
Persistent=true

[Install]
WantedBy=timers.target
EOF
systemctl daemon-reload
systemctl enable --now logicup-backup.timer >/dev/null
bash "$PASTA_APP/backup.sh"
ok "backup diário ligado (próximo: $(systemctl show logicup-backup.timer -p NextElapseUSecRealtime --value))"

echo
ok "LogiCup no ar. Próximos passos (DEPLOY.md): criar o administrador (sudo bash criar_admin.sh) e testar https://$dominio"
