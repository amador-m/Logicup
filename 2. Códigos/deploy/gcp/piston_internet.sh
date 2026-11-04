#!/bin/bash
# =====================================================================
# LogiCup — liga/desliga a INTERNET da VM do Piston. Rode no Cloud Shell.
#
#   bash piston_internet.sh desligar   # estado normal de produção (depois da instalação)
#   bash piston_internet.sh ligar      # manutenção: atualizar o Ubuntu/Docker/Piston; depois DESLIGUE de novo
#
# "desligar" faz duas coisas (duas camadas):
#   1. remove o IP externo da VM: ela deixa de existir na internet (ninguém de fora a alcança);
#   2. liga a regra de firewall logicup-piston-sem-internet: mesmo que alguém recoloque um IP, a VM
#      não consegue abrir conexão para fora. A logicup-app continua chegando na porta 2000.
# O SSH pelo console continua funcionando (passa pelo IAP do Google, pela rede interna).
# =====================================================================
set -euo pipefail
ZONA="southamerica-east1-b"
VM="logicup-piston"
ok() { printf '\033[1;32mOK\033[0m  %s\n' "$*"; }

acesso="$(gcloud compute instances describe "$VM" --zone="$ZONA" --format='value(networkInterfaces[0].accessConfigs[0].name)')"

case "${1:-}" in
    desligar)
        if [[ -n "$acesso" ]]; then
            gcloud compute instances delete-access-config "$VM" --zone="$ZONA" --access-config-name="$acesso"
        fi
        ok "IP externo removido"
        gcloud compute firewall-rules update logicup-piston-sem-internet --no-disabled
        ok "saída para a internet BLOQUEADA"
        ;;
    ligar)
        gcloud compute firewall-rules update logicup-piston-sem-internet --disabled
        ok "bloqueio de saída desligado"
        if [[ -z "$acesso" ]]; then
            gcloud compute instances add-access-config "$VM" --zone="$ZONA"
        fi
        ok "IP externo temporário adicionado. LEMBRE de rodar 'bash piston_internet.sh desligar' ao terminar."
        ;;
    *)
        echo "uso: bash piston_internet.sh desligar|ligar"; exit 1 ;;
esac
