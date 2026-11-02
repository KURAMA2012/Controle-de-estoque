-- Schema e cadastros auxiliares do item (uma tabela por tipo)
CREATE SCHEMA IF NOT EXISTS apl_estoque;

CREATE TABLE IF NOT EXISTS apl_estoque.tab_categoria (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL
);
COMMENT ON TABLE apl_estoque.tab_categoria IS 'Cadastro auxiliar: categoria do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_marca (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL
);
COMMENT ON TABLE apl_estoque.tab_marca IS 'Cadastro auxiliar: marca do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_unidade (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL
);
COMMENT ON TABLE apl_estoque.tab_unidade IS 'Cadastro auxiliar: unidade do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_cor (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL
);
COMMENT ON TABLE apl_estoque.tab_cor IS 'Cadastro auxiliar: cor do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_tamanho (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL
);
COMMENT ON TABLE apl_estoque.tab_tamanho IS 'Cadastro auxiliar: tamanho do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_modelo (
    id SERIAL PRIMARY KEY,
    ds_descricao VARCHAR(255) NOT NULL,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL,
    id_marca INT,
    CONSTRAINT fk_modelo_marca FOREIGN KEY (id_marca) REFERENCES apl_estoque.tab_marca (id)
);
CREATE INDEX IF NOT EXISTS idx_modelo_marca ON apl_estoque.tab_modelo (id_marca);
COMMENT ON TABLE apl_estoque.tab_modelo IS 'Cadastro auxiliar: modelo do item (opcionalmente vinculado a uma marca)';