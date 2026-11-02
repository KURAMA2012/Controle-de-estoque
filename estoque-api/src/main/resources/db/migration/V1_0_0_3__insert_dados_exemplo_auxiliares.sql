-- Dados de exemplo dos cadastros auxiliares (banco novo/vazio)
INSERT INTO apl_estoque.tab_categoria (ds_descricao) VALUES
    ('Notebooks'), ('Smartphones'), ('Tablets'), ('Monitores'), ('Roupas');

INSERT INTO apl_estoque.tab_marca (ds_descricao) VALUES
    ('Samsung'), ('Apple'), ('Dell'), ('Lenovo'), ('LG');

INSERT INTO apl_estoque.tab_modelo (ds_descricao, id_marca) VALUES
    ('Galaxy S24 Ultra', (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = 'Samsung')),
    ('Galaxy Tab S9',    (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = 'Samsung')),
    ('iPhone 15',        (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = 'Apple')),
    ('Inspiron 15',      (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = 'Dell')),
    ('IdeaPad 3',        (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = 'Lenovo'));

INSERT INTO apl_estoque.tab_unidade (ds_descricao) VALUES
    ('UN'), ('CX'), ('KG'), ('PC'), ('M');

INSERT INTO apl_estoque.tab_cor (ds_descricao) VALUES
    ('Preto'), ('Branco'), ('Cinza'), ('Azul'), ('Vermelho');

INSERT INTO apl_estoque.tab_tamanho (ds_descricao) VALUES
    ('PP'), ('P'), ('M'), ('G'), ('GG');