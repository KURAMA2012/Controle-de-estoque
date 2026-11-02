# ---- build ----
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# ---- runtime ----
FROM nginx:1.27-alpine
# Template: o entrypoint do nginx troca ${PORT} (Render/Railway informam PORT; padrao 80)
ENV PORT=80
COPY nginx.conf /etc/nginx/templates/default.conf.template
RUN rm -f /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/estoque-web/browser /usr/share/nginx/html
# Gera /config.js a partir de API_URL, DEMO_LOGIN e DEMO_SENHA no start
COPY --chmod=755 docker/40-estoque-config.sh /docker-entrypoint.d/40-estoque-config.sh
EXPOSE 80
