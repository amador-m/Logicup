#!/bin/bash
# =====================================================================
# LogiCup — cria o PRIMEIRO administrador em produção (use uma vez, depois do instalar.sh).
#   sudo bash criar_admin.sh
#
# Pede o e-mail e a senha no terminal (a senha não aparece enquanto você digita) e roda o backend
# com o perfil "criar-admin": ele cria o ADMIN com as MESMAS regras do sistema (e-mail normalizado,
# senha com BCrypt) e termina. A senha não fica gravada em arquivo nenhum.
# O username do admin é "admin". No primeiro login, o site pede o aceite dos Termos, como para todos.
# =====================================================================
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comum.sh"
precisa_root
precisa_env

read -r -p "E-mail do administrador: " email
[[ "$email" =~ ^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$ ]] || falha "e-mail inválido"
read -r -s -p "Senha (mínimo 12 caracteres): " senha; echo
(( ${#senha} >= 12 )) || falha "a senha precisa ter pelo menos 12 caracteres"
read -r -s -p "Repita a senha: " senha2; echo
[[ "$senha" == "$senha2" ]] || falha "as senhas não são iguais"

info "Criando o administrador (leva uns 20 segundos)"
# As variáveis vão só para este contêiner temporário (--rm: ele é apagado ao terminar)
ADMIN_EMAIL="$email" ADMIN_SENHA="$senha" compose run --rm --no-deps \
    -e SPRING_PROFILES_ACTIVE=prod,criar-admin -e ADMIN_EMAIL -e ADMIN_SENHA \
    backend 2>&1 | grep -E "ADMIN|ERROR|Exception" || true
unset senha senha2

conta="$(echo "SELECT COUNT(*) FROM Logicup.usuario WHERE tipo_usuario = 'ADMIN';" \
    | compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot -N')"
(( conta >= 1 )) || falha "nenhum ADMIN no banco: veja a mensagem acima"
ok "administrador pronto (total de ADMIN no banco: $conta). Entre no site com o e-mail ou com o username admin."
