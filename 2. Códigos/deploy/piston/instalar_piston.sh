#!/bin/bash
# =====================================================================
# LogiCup — PREPARA a VM "logicup-piston" do zero. Rode na VM nova, ENQUANTO ela ainda tem internet
# (IP externo temporário; ver DEPLOY.md, passo 4):
#   sudo bash instalar_piston.sh
#
#  1. instala Docker (pacotes do Ubuntu)
#  2. sobe o Piston com os limites do LogiCup
#  3. instala o Java 15.0.2 no Piston (baixado da internet: por isso a VM precisa de rede AGORA)
#  4. aplica o ajuste de UTF-8: sem ele, "café" vira "caf?" e alunos que acertaram seriam reprovados
#     (descoberto no teste real de 02/11; ver ExecutorPistonRealTest.acentosFuncionamNaLeituraENaSaida)
#  5. testa: Olá mundo com acento, e um programa que tenta acessar a internet (precisa FALHAR)
# Depois disso, o DEPLOY.md manda tirar o IP externo e bloquear a saída para a internet.
# =====================================================================
set -euo pipefail
PASTA="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
URL="http://127.0.0.1:2000"
info()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
ok()    { printf '\033[1;32mOK\033[0m  %s\n' "$*"; }
falha() { printf '\033[1;31mERRO\033[0m %s\n' "$*" >&2; exit 1; }
[[ $EUID -eq 0 ]] || falha "rode com sudo: sudo $0"

info "1/5 Docker"
timedatectl set-timezone America/Sao_Paulo
if ! command -v docker >/dev/null || ! docker compose version >/dev/null 2>&1; then
    apt-get update -q
    DEBIAN_FRONTEND=noninteractive apt-get install -y -q docker.io docker-compose-v2 jq
fi
command -v jq >/dev/null || DEBIAN_FRONTEND=noninteractive apt-get install -y -q jq
systemctl enable --now docker >/dev/null
ok "$(docker --version)"

info "2/5 Subindo o Piston"
docker compose --project-directory "$PASTA" up -d
for _ in $(seq 1 30); do curl -sf "$URL/api/v2/runtimes" >/dev/null && break; sleep 2; done
curl -sf "$URL/api/v2/runtimes" >/dev/null || falha "o Piston não respondeu; veja: sudo docker compose logs"
# registra a versão exata da imagem (para fixar no compose depois, se quiser)
docker image inspect --format '{{index .RepoDigests 0}}' ghcr.io/engineer-man/piston:latest > "$PASTA/versao-imagem.txt"
ok "Piston no ar ($(cat "$PASTA/versao-imagem.txt"))"

info "3/5 Java 15.0.2"
if curl -sf "$URL/api/v2/runtimes" | jq -e '.[] | select(.language == "java" and .version == "15.0.2")' >/dev/null; then
    ok "já instalado"
else
    curl -sf -X POST "$URL/api/v2/packages" -H 'Content-Type: application/json' \
        -d '{"language":"java","version":"15.0.2"}' | jq -c . || falha "não consegui instalar o Java"
    ok "instalado"
fi

info "4/5 Ajuste de UTF-8 no script de execução do Java"
run="$PASTA/dados/packages/java/15.0.2/run"
[[ -f "$run" ]] || falha "não achei $run"
if grep -q -- '-Dfile.encoding=UTF-8' "$run"; then
    ok "já aplicado"
else
    sed -i 's|^java \$filename|java -Dfile.encoding=UTF-8 $filename|' "$run"
    grep -q -- '-Dfile.encoding=UTF-8' "$run" || falha "o ajuste não foi aplicado (o formato do $run mudou?)"
    ok "aplicado"
fi

info "5/5 Testes"
executar() {   # $1 = corpo do main; devolve a saída (stdout) do programa
    jq -n --arg c "public class Main { public static void main(String[] a) throws Exception { $1 } }" \
        '{language:"java", version:"15.0.2", files:[{name:"Main", content:$c}]}' \
      | curl -sf -X POST "$URL/api/v2/execute" -H 'Content-Type: application/json' -d @- \
      | jq -r '.run.stdout'
}
saida="$(executar 'System.out.println("Olá, Gattuccino! café");')"
[[ "$saida" == "Olá, Gattuccino! café" ]] || falha "acentos errados: '$saida'"
ok "acentos: $saida"
saida="$(executar 'try { new java.net.URL("https://www.google.com").openStream(); System.out.println("ACESSOU"); } catch (Exception e) { System.out.println("BLOQUEADO"); }')"
[[ "$saida" == "BLOQUEADO" ]] || falha "um programa de aluno conseguiu acessar a internet! ($saida)"
ok "sandbox sem rede: programa de aluno não acessa a internet"

echo
ok "Piston pronto. Volte ao DEPLOY.md: tirar o IP externo desta VM e ligar o bloqueio de saída."
