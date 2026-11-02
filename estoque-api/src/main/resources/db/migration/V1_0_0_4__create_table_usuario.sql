-- Usuarios da API (autenticacao JWT propria). Sem senha semeada: o primeiro ADMIN e criado
-- na subida da aplicacao a partir de APP_ADMIN_LOGIN / APP_ADMIN_SENHA, se a tabela estiver vazia.
CREATE TABLE IF NOT EXISTS apl_estoque.tab_usuario (
    id SERIAL PRIMARY KEY,
    ds_login VARCHAR(100) NOT NULL,
    ds_senha VARCHAR(100) NOT NULL,
    ds_nome VARCHAR(255),
    ds_perfil VARCHAR(30) DEFAULT 'USER' NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL,
    dt_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_usuario_login UNIQUE (ds_login),
    CONSTRAINT ck_usuario_perfil CHECK (ds_perfil IN ('ADMIN', 'USER'))
);
COMMENT ON TABLE apl_estoque.tab_usuario IS 'Usuarios da API de estoque';
COMMENT ON COLUMN apl_estoque.tab_usuario.ds_senha IS 'Hash BCrypt da senha';