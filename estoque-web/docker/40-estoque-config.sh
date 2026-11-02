#!/bin/sh
# Gera /config.js a partir das variaveis de ambiente do container (API_URL, DEMO_LOGIN, DEMO_SENHA).
set -e
esc() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }
cat > /usr/share/nginx/html/config.js <<JS
window.__ESTOQUE_CONFIG__ = {
  apiUrl: "$(esc "${API_URL:-}")",
  demoLogin: "$(esc "${DEMO_LOGIN:-}")",
  demoSenha: "$(esc "${DEMO_SENHA:-}")"
};
JS
