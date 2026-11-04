# =====================================================================
# LogiCup — imagem do FRONTEND + servidor web (deploy, 03/11/2026)
#
# Em produção não existe "npm run dev": o Vite gera arquivos estáticos (HTML, JS, CSS, imagens) na
# pasta dist/, e quem entrega esses arquivos é o Caddy, o mesmo servidor que faz o HTTPS e repassa
# o /api para o backend. A configuração dele (Caddyfile) fica em deploy/app/ e entra por volume.
#
#   1) build: Node instala as dependências e roda "npm run build"
#   2) final: imagem do Caddy + a pasta dist/ (sem Node, sem node_modules, sem código-fonte)
# =====================================================================

# ---------- etapa 1: gerar o site ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
# "npm ci" instala EXATAMENTE as versões do package-lock.json (build reproduzível)
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# ---------- etapa 2: servidor web ----------
FROM caddy:2.10-alpine
COPY --from=build /app/dist /srv
