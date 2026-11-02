# Estoque API

API REST de **estoque e catálogo de produtos** em Spring Boot. Permite cadastrar itens com preços, estoque, galeria de imagens (upload ou por URL), especificações técnicas, cores e tamanhos, além dos cadastros auxiliares (categoria, marca, modelo, unidade, cor e tamanho). O acesso é protegido por autenticação **JWT** própria.

## Stack

- Java 8
- Spring Boot 2.5.15 (Web, Data JPA, Security)
- PostgreSQL 16
- Flyway (migrations versionadas)
- Spring Security + jjwt 0.9.1 (HS512) + BCrypt
- Docker / Docker Compose

## Como executar

### Opção 1: Docker Compose (recomendado)

```bash
# na raiz do monorepo (estoque/), onde ficam docker-compose.yml e .env.example
cd ..
cp .env.example .env
# edite o .env: DB_PASSWORD, JWT_SECRET (>= 64 bytes) e APP_ADMIN_SENHA
docker compose up -d --build
```

Sobem dois serviços: `db` (PostgreSQL 16 com volume `estoque-pgdata` e healthcheck) e `api` (build de `estoque-api/Dockerfile`), que só inicia depois que o banco estiver saudável. A API fica em `http://localhost:7009/api/estoque`.

Para parar: `docker compose down` (adicione `-v` para apagar também os dados).

> Variáveis definidas no terminal têm prioridade sobre o `.env` na interpolação do Compose. Se o login no banco falhar, verifique se não há `DB_USER`/`DB_PASSWORD` exportados na sua sessão.

### Opção 2: Local com Maven Wrapper

Requer JDK 8+ e um PostgreSQL acessível com o banco `estoque` criado.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/estoque
export DB_USER=postgres
export DB_PASSWORD=sua-senha
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
export APP_ADMIN_LOGIN=admin
export APP_ADMIN_SENHA=uma-senha-forte

./mvnw spring-boot:run
# ou: ./mvnw clean package -DskipTests && java -jar target/estoque-api-1.0.0-SNAPSHOT.jar
```

No Windows use `mvnw.cmd` e `$env:NOME="valor"` no PowerShell.

## Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DB_URL` | não | `jdbc:postgresql://localhost:5432/estoque` | URL JDBC (no Compose aponta para o serviço `db`) |
| `DB_USER` | sim | | Usuário do PostgreSQL |
| `DB_PASSWORD` | sim | | Senha do PostgreSQL |
| `JWT_SECRET` | sim | | Segredo HS512, **mínimo 64 bytes**. A aplicação não sobe sem ele |
| `JWT_EXPIRATION` | não | `86400000` | Validade do token em ms (24 h) |
| `APP_ADMIN_LOGIN` | não | | Login do primeiro ADMIN (criado só se não houver usuários) |
| `APP_ADMIN_SENHA` | não | | Senha do primeiro ADMIN |
| `APP_CORS_ALLOWED_ORIGINS` | não | `http://localhost:4300` | Origens CORS permitidas, separadas por vírgula |
| `ESTOQUE_PORT` | não | `7009` | Porta HTTP |
| `LOG_FILE` | não | `estoque-api.log` | Caminho do arquivo de log |
| `DATABASE_MAX_CONNECTION` | não | | Tamanho máximo do pool Hikari |
| `DATABASE_MIN_IDLE` | não | | Conexões ociosas mínimas do pool |
| `ESTOQUE_IMAGEM_HOSTS_PERMITIDOS` | não | | Hosts liberados da checagem de rede interna para imagens por URL |
| `JAVA_OPTS` | não | `-Xms256m -Xmx512m` | Opções da JVM (apenas Docker) |

Gerar um `JWT_SECRET`:

```bash
openssl rand -base64 64 | tr -d '\n'
```

```powershell
[Convert]::ToBase64String((1..64 | % { Get-Random -Max 256 }))
```

## Migrations (Flyway)

Ficam em `src/main/resources/db/migration` e rodam automaticamente na subida, no schema `apl_estoque`:

| Versão | Conteúdo |
|---|---|
| `V1_0_0_1` | Schema e tabelas auxiliares (categoria, marca, modelo, unidade, cor, tamanho) |
| `V1_0_0_2` | Tabelas de item, galeria de imagens, especificações e vínculos item-cor / item-tamanho |
| `V1_0_0_3` | Dados de exemplo dos cadastros auxiliares |
| `V1_0_0_4` | Tabela de usuários (`tab_usuario`), sem senha semeada |

## Modelo de dados (resumo)

- **Cadastros auxiliares** (`tab_categoria`, `tab_marca`, `tab_unidade`, `tab_cor`, `tab_tamanho`): `id`, `descricao`, `ativo`.
- **Modelo** (`tab_modelo`): igual aos auxiliares + `marca` (FK). Um modelo pertence a uma marca.
- **Item**: nome, código de barras, descrição, validade, unidade, categoria, marca, modelo, estoque (mínimo, máximo, atual), valores (custo, margem, venda, preço antigo), parcelas, flags (`ativo`, `full`, `freteGratis`), rating, reviews, vendidos, código do fornecedor, imagem principal, cores e tamanhos (N:N).
- **ItemImagem**: galeria do item (bytes da imagem, ordem, URL de origem).
- **ItemEspecificacao**: pares `chave` / `valor` do item.
- **Usuario**: login (único), senha (hash BCrypt), nome, perfil (`ADMIN` ou `USER`), ativo.

## Endpoints

Prefixo: `/api/estoque`. Todos exigem `Authorization: Bearer <token>`, exceto `POST /auth/login`.

### Autenticação

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/auth/login` | `{login, senha}` → `{token, tipo, expiraEm, usuario}` |
| GET | `/auth/me` | Dados do usuário autenticado |

### Item (`/item`)

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/inserir` | Cria item (corpo com `idUnidade`, `idCategoria`, `idMarca`, `idModelo`, `idsCores`, `idsTamanhos`...) |
| PUT | `/{id}/atualizar` | Atualiza item |
| GET | `/listar` | Lista todos |
| GET | `/buscar/{id}` | Busca por id |
| GET | `/detalhar/{id}` | Detalhe do item |
| DELETE | `/deletar/{id}` | Remove item |
| POST | `/consultar` | Consulta paginada com filtros (`{page, size, orderBy, filtros}`) |
| POST | `/inserir-entidade` | Cria a partir da entidade completa |
| PUT | `/{id}/atualizar-entidade` | Atualiza a partir da entidade completa |
| POST | `/anexar-imagem/{idItem}` | Upload multipart (campo `arquivo`) para a galeria |
| POST | `/{idItem}/imagens/url` | Adiciona imagem baixada de uma URL (`{url}`) |
| GET | `/{idItem}/imagens` | Lista a galeria |
| DELETE | `/imagens/{idImagem}` | Remove imagem |
| PUT | `/imagens/{idImagem}/definir-principal` | Define a imagem principal |
| GET | `/{idItem}/especificacoes` | Lista especificações |
| POST | `/{idItem}/especificacoes` | Adiciona especificação (`{chave, valor}`) |
| PUT | `/especificacoes/{id}` | Atualiza especificação |
| DELETE | `/especificacoes/{id}` | Remove especificação |

### Cadastros auxiliares

Recursos: `/categoria`, `/marca`, `/modelo`, `/unidade`, `/cor`, `/tamanho`.

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/{recurso}/listar` | Lista em ordem alfabética (`?ativos=true` filtra ativos) |
| GET | `/{recurso}/buscar/{id}` | Busca por id |
| POST | `/{recurso}/inserir` | Cria (`{descricao, ativo}`; modelo também recebe `marca: {id}`) |
| PUT | `/{recurso}/{id}/atualizar` | Atualiza |
| DELETE | `/{recurso}/deletar/{id}` | Remove (409 se estiver em uso) |
| GET | `/modelo/listar-por-marca/{idMarca}` | Modelos de uma marca |

## Exemplos com curl

```bash
# login
TOKEN=$(curl -s -X POST http://localhost:7009/api/estoque/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","senha":"sua-senha"}' | jq -r .token)

# listar marcas
curl -s http://localhost:7009/api/estoque/marca/listar \
  -H "Authorization: Bearer $TOKEN"

# criar item
curl -s -X POST http://localhost:7009/api/estoque/item/inserir \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nome":"Galaxy S24 Ultra 256GB","idCategoria":2,"idMarca":1,"idModelo":1,"idUnidade":1,"idsCores":[1],"idsTamanhos":[],"ativo":true}'
```

Resposta do login:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-02T09:40:06.198Z",
  "usuario": { "id": 1, "login": "admin", "nome": "Administrador", "perfil": "ADMIN" }
}
```

## Formato de erro

Todos os erros seguem o mesmo corpo JSON:

```json
{
  "codeError": 409,
  "requestedURI": "/api/estoque",
  "dataHora": "01/11/2026 06:40:06",
  "message": "Marca está em uso e não pode ser removido(a)."
}
```

| Status | Quando |
|---|---|
| 401 | Sem token, token inválido/expirado ou login/senha inválidos |
| 403 | Acesso negado |
| 409 | Regra de negócio violada (registro inexistente, modelo de outra marca, registro em uso, imagem inválida...) |

## Regras de segurança das imagens

- Formatos aceitos: **JPEG, PNG, WebP e GIF** (o `Content-Type` e os bytes do arquivo são validados).
- Tamanho máximo: **10 MB** por imagem.
- Máximo de **10 imagens** por item.
- Imagens por URL (proteção contra **SSRF**):
  - apenas `http` e `https`, sem credenciais na URL;
  - o host é resolvido e são bloqueados endereços internos: loopback, rede privada, link-local, multicast, "any local", IPv6 ULA e IPv4 mapeado em IPv6;
  - redirecionamentos são seguidos manualmente (máximo 5) e cada destino é validado de novo;
  - timeouts de conexão (10 s) e leitura (20 s);
  - hosts confiáveis podem ser liberados em `ESTOQUE_IMAGEM_HOSTS_PERMITIDOS`.

## Testes

O projeto ainda não possui testes automatizados.

## Licença

Distribuído sob a licença [MIT](../LICENSE).

## Autor

Davy Lucas
