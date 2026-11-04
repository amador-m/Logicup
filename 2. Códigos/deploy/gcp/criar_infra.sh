#!/bin/bash
# =====================================================================
# LogiCup — cria a infraestrutura no Google Cloud (deploy, 03/11/2026).
# Rode no CLOUD SHELL (o terminal do próprio site do Google Cloud, botão ">_" no topo), NÃO na VM:
#   bash criar_infra.sh
# Pode rodar de novo: o que já existe é pulado.
#
# O que é criado (tudo em São Paulo, southamerica-east1):
#   rede própria "logicup-rede" (10.10.0.0/24), sem as regras abertas da rede "default"
#   IP público fixo           -> o domínio aponta para ele; não muda se a VM reiniciar
#   VM logicup-app            -> e2-medium (2 vCPU compartilhadas, 4 GB): Caddy + backend + MySQL
#   VM logicup-piston         -> e2-small (2 vCPU compartilhadas, 2 GB): só o Piston
#   firewall                  -> 80/443 abertos só na logicup-app; porta 2000 do Piston só para a
#                                logicup-app; SSH só pelo IAP do Google (nenhuma porta 22 aberta na internet)
#   snapshot diário do disco da logicup-app, guardado 7 dias, no Brasil
#
# CUSTO (consome o crédito de teste; estimativa de 03/11): ~US$ 73/mês, ~US$ 220 em 90 dias.
# =====================================================================
set -euo pipefail

REGIAO="southamerica-east1"
ZONA="southamerica-east1-b"
IMAGEM=(--image-family=ubuntu-2404-lts-amd64 --image-project=ubuntu-os-cloud)
SEGURANCA=(--shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --metadata=enable-oslogin=TRUE)

info() { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
ok()   { printf '\033[1;32mOK\033[0m  %s\n' "$*"; }
# roda "gcloud <recurso> create ..." só se "gcloud <recurso> describe <nome>" falhar (não existe ainda)
criar() {
    local recurso="$1" nome="$2" local_flag="$3"; shift 3
    # shellcheck disable=SC2086  # local_flag pode ser vazio de propósito
    if gcloud $recurso describe "$nome" $local_flag >/dev/null 2>&1; then
        ok "$nome já existe"
    else
        # shellcheck disable=SC2086
        gcloud $recurso create "$nome" $local_flag "$@" && ok "$nome criado"
    fi
}

PROJETO="$(gcloud config get-value project 2>/dev/null)"
[[ -n "$PROJETO" ]] || { echo "Escolha o projeto antes: gcloud config set project <ID>"; exit 1; }
info "Projeto: $PROJETO"
gcloud services enable compute.googleapis.com iap.googleapis.com

info "1/6 Rede"
criar "compute networks" logicup-rede "" --subnet-mode=custom
criar "compute networks subnets" logicup-sp "--region=$REGIAO" --network=logicup-rede --range=10.10.0.0/24

info "2/6 Firewall"
criar "compute firewall-rules" logicup-web "" --network=logicup-rede --direction=INGRESS --action=ALLOW \
    --rules=tcp:80,tcp:443,udp:443 --source-ranges=0.0.0.0/0 --target-tags=logicup-app
# 35.235.240.0/20 = endereços do IAP do Google (o botão "SSH" do console passa por eles)
criar "compute firewall-rules" logicup-ssh-iap "" --network=logicup-rede --direction=INGRESS --action=ALLOW \
    --rules=tcp:22 --source-ranges=35.235.240.0/20 --target-tags=logicup-app,logicup-piston
criar "compute firewall-rules" logicup-piston-interno "" --network=logicup-rede --direction=INGRESS --action=ALLOW \
    --rules=tcp:2000 --source-tags=logicup-app --target-tags=logicup-piston
# Bloqueio de SAÍDA da VM do Piston para a internet. Nasce DESLIGADO (--disabled) porque a instalação
# precisa baixar o Java; o DEPLOY.md manda ligar depois. As respostas para a logicup-app continuam
# funcionando (o firewall do Google é "stateful": resposta de conexão aceita sempre passa).
criar "compute firewall-rules" logicup-piston-sem-internet "" --network=logicup-rede --direction=EGRESS --action=DENY \
    --rules=all --destination-ranges=0.0.0.0/0 --target-tags=logicup-piston --priority=1000 --disabled

info "3/6 IP público fixo"
criar "compute addresses" logicup-ip "--region=$REGIAO"
IP="$(gcloud compute addresses describe logicup-ip --region="$REGIAO" --format='value(address)')"
ok "IP público: $IP"

info "4/6 VMs"
criar "compute instances" logicup-app "--zone=$ZONA" --machine-type=e2-medium \
    --network-interface="subnet=logicup-sp,address=$IP" --tags=logicup-app \
    "${IMAGEM[@]}" --boot-disk-size=30GB --boot-disk-type=pd-balanced "${SEGURANCA[@]}"
# o Piston nasce com IP externo TEMPORÁRIO (efêmero) só para a instalação; depois ele é removido
criar "compute instances" logicup-piston "--zone=$ZONA" --machine-type=e2-small \
    --network-interface="subnet=logicup-sp" --tags=logicup-piston \
    "${IMAGEM[@]}" --boot-disk-size=20GB --boot-disk-type=pd-balanced "${SEGURANCA[@]}"

info "5/6 Snapshot diário do disco da logicup-app"
# 06:00 UTC = 03:00 em Brasília. Guardado na região de São Paulo (dados dos alunos ficam no Brasil).
if gcloud compute resource-policies describe logicup-snapshot-diario --region="$REGIAO" >/dev/null 2>&1; then
    ok "logicup-snapshot-diario já existe"
else
    gcloud compute resource-policies create snapshot-schedule logicup-snapshot-diario --region="$REGIAO" \
        --daily-schedule --start-time=06:00 --max-retention-days=7 \
        --storage-location="$REGIAO" --on-source-disk-delete=keep-auto-snapshots
    ok "logicup-snapshot-diario criado"
fi
if gcloud compute disks describe logicup-app --zone="$ZONA" --format='value(resourcePolicies)' | grep -q logicup-snapshot-diario; then
    ok "snapshot já ligado no disco"
else
    gcloud compute disks add-resource-policies logicup-app --zone="$ZONA" --resource-policies=logicup-snapshot-diario
    ok "snapshot ligado no disco"
fi

info "6/6 Resumo"
IP_PISTON="$(gcloud compute instances describe logicup-piston --zone="$ZONA" --format='value(networkInterfaces[0].networkIP)')"
echo
echo "  IP PÚBLICO do site (vai no DNS do Registro.br): $IP"
echo "  IP INTERNO do Piston (o instalar.sh da logicup-app pergunta): $IP_PISTON"
echo
ok "Infraestrutura pronta. Volte ao DEPLOY.md, passo 2."
