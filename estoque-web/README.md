# estoque-web

Front-end Angular do sistema de estoque. Consome a [estoque-api](../estoque-api/README.md).

## Stack

- Angular 22 (componentes standalone, signals, controle de fluxo `@if`/`@for`)
- SCSS puro com design system em variáveis CSS (sem biblioteca de UI)
- Ícones SVG inline
- Nginx para servir o build em produção (Docker)

## Funcionalidades (etapa 1: fundação)

- **Login** em `POST /auth/login`; o token fica no `localStorage`.
- **Interceptor HTTP** que envia `Authorization: Bearer <token>` para a API; resposta `401` faz logout e volta para o login.
- **Guards**: rotas internas exigem login; `/login` redireciona quem já está autenticado.
- **Usuário logado** carregado de `GET /auth/me` (nome e iniciais no menu do topo).
- **Layout**: sidebar escura recolhível (Dashboard, Produtos, Cadastros: Categorias, Marcas, Modelos, Unidades, Cores, Tamanhos), barra superior com busca, alternância de tema e menu do usuário com **Sair**.
- **Tema claro/escuro** (`ThemeService`), salvo em `localStorage` (`estoque.tema`). Sem preferência salva, segue o sistema.
- **Modos de visualização de produtos** (`ViewModeService`), salvos em `localStorage` (`estoque.visualizacao`):
  - `tabela` (A): tabela clássica;
  - `cards` (B): grade de cards com painel de filtros (categoria, marca, cor, tamanho, faixa de preço);
  - `detalhe` (C): mestre-detalhe com indicadores (Total de produtos, Sem estoque, Valor em estoque).

  Nesta etapa a página Produtos mostra o seletor (controle segmentado com ícones) e um placeholder de cada modo.

## Estrutura

```
src/
├── environments/environment.ts   # apiUrl
├── styles.scss                    # design system (temas, componentes base, layout)
└── app/
    ├── core/      # AuthService, interceptor, guards, ThemeService, ViewModeService
    ├── layout/    # ShellComponent (sidebar + topbar)
    ├── shared/    # ícones, seletor de visualização
    └── pages/     # login, dashboard, produtos, cadastro
```

## Executar em desenvolvimento

Requer Node 22.22+ ou 24.15+ e a API rodando em `http://localhost:7009/api/estoque` (por exemplo com `docker compose up -d db api` na raiz do monorepo).

```bash
cd estoque-web
npm install
npm start
```

Abra `http://localhost:4300`. A API já libera CORS para essa origem (`APP_CORS_ALLOWED_ORIGINS`).

Para apontar para outra API, altere `apiUrl` em `src/environments/environment.ts`.

## Build

```bash
npm run build   # saída em dist/estoque-web/browser
```

## Docker

O `Dockerfile` faz o build com Node e serve os arquivos com Nginx na porta 80, com fallback de SPA para `index.html`. Na raiz do monorepo:

```bash
docker compose up -d --build
```

O front fica em `http://localhost:4300`.

## Licença

[MIT](../LICENSE)
