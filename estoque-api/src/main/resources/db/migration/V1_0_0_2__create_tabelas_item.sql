-- Item, galeria de imagens, especificacoes e juncoes cor/tamanho
CREATE TABLE IF NOT EXISTS apl_estoque.tab_item (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    id_unidade INT,
    id_categoria INT,
    id_marca INT,
    id_modelo INT,
    codigo_barras VARCHAR(100),
    imagem BYTEA,
    nm_icone VARCHAR(255),
    descricao TEXT,
    data_validade DATE,
    quantidade_estoque_minimo INT DEFAULT 0,
    quantidade_estoque_maximo INT DEFAULT 0,
    quantidade_estoque_atual INT DEFAULT 0,
    fl_ativo BOOLEAN DEFAULT TRUE NOT NULL,
    vlr_custo NUMERIC(15, 2),
    vlr_margem_lucro NUMERIC(15, 2),
    vlr_venda NUMERIC(15, 2),
    vlr_preco_antigo NUMERIC(15, 2),
    nr_max_parcelas INT,
    fl_full BOOLEAN DEFAULT FALSE NOT NULL,
    fl_frete_gratis BOOLEAN DEFAULT FALSE NOT NULL,
    vlr_rating NUMERIC(5, 2),
    qtd_reviews INT,
    qtd_vendidos INT,
    cd_fornecedor VARCHAR(100),
    CONSTRAINT fk_item_unidade FOREIGN KEY (id_unidade) REFERENCES apl_estoque.tab_unidade (id),
    CONSTRAINT fk_item_categoria FOREIGN KEY (id_categoria) REFERENCES apl_estoque.tab_categoria (id),
    CONSTRAINT fk_item_marca FOREIGN KEY (id_marca) REFERENCES apl_estoque.tab_marca (id),
    CONSTRAINT fk_item_modelo FOREIGN KEY (id_modelo) REFERENCES apl_estoque.tab_modelo (id)
);
CREATE INDEX IF NOT EXISTS idx_item_categoria ON apl_estoque.tab_item (id_categoria);
CREATE INDEX IF NOT EXISTS idx_item_marca ON apl_estoque.tab_item (id_marca);
CREATE INDEX IF NOT EXISTS idx_item_modelo ON apl_estoque.tab_item (id_modelo);
COMMENT ON TABLE apl_estoque.tab_item IS 'Dados fisicos, comerciais e de controle de estoque dos itens';
COMMENT ON COLUMN apl_estoque.tab_item.nm_icone IS 'Nome/caminho do icone principal (vitrine)';
COMMENT ON COLUMN apl_estoque.tab_item.vlr_preco_antigo IS 'Preco anterior para exibicao de desconto';
COMMENT ON COLUMN apl_estoque.tab_item.vlr_rating IS 'Avaliacao media do produto (0 a 5)';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_item_imagem (
    id SERIAL PRIMARY KEY,
    id_item INT NOT NULL,
    imagem BYTEA,
    nr_ordem INT,
    ds_url VARCHAR(2000),
    CONSTRAINT fk_item_imagem_item FOREIGN KEY (id_item) REFERENCES apl_estoque.tab_item (id)
);
CREATE INDEX IF NOT EXISTS idx_item_imagem_item ON apl_estoque.tab_item_imagem (id_item);
COMMENT ON TABLE apl_estoque.tab_item_imagem IS 'Galeria de imagens adicionais do item';
COMMENT ON COLUMN apl_estoque.tab_item_imagem.ds_url IS 'URL de origem da imagem, quando adicionada por URL';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_item_especificacao (
    id SERIAL PRIMARY KEY,
    id_item INT NOT NULL,
    nm_chave VARCHAR(255),
    ds_valor TEXT,
    CONSTRAINT fk_item_especificacao_item FOREIGN KEY (id_item) REFERENCES apl_estoque.tab_item (id)
);
CREATE INDEX IF NOT EXISTS idx_item_especificacao_item ON apl_estoque.tab_item_especificacao (id_item);
COMMENT ON TABLE apl_estoque.tab_item_especificacao IS 'Especificacoes tecnicas chave/valor do item';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_item_cor (
    id_item INT NOT NULL,
    id_cor INT NOT NULL,
    PRIMARY KEY (id_item, id_cor),
    CONSTRAINT fk_item_cor_item FOREIGN KEY (id_item) REFERENCES apl_estoque.tab_item (id),
    CONSTRAINT fk_item_cor_cor FOREIGN KEY (id_cor) REFERENCES apl_estoque.tab_cor (id)
);
COMMENT ON TABLE apl_estoque.tab_item_cor IS 'Relacao item x cor';

CREATE TABLE IF NOT EXISTS apl_estoque.tab_item_tamanho (
    id_item INT NOT NULL,
    id_tamanho INT NOT NULL,
    PRIMARY KEY (id_item, id_tamanho),
    CONSTRAINT fk_item_tamanho_item FOREIGN KEY (id_item) REFERENCES apl_estoque.tab_item (id),
    CONSTRAINT fk_item_tamanho_tamanho FOREIGN KEY (id_tamanho) REFERENCES apl_estoque.tab_tamanho (id)
);
COMMENT ON TABLE apl_estoque.tab_item_tamanho IS 'Relacao item x tamanho';