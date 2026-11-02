-- Deixa os dados de exemplo coerentes (marca/modelo/categoria) e semeia produtos em banco vazio.
-- Idempotente: pode rodar em banco novo ou em banco que ja tem os produtos de exemplo.

-- 1) Marcas e modelos de vestuario
INSERT INTO apl_estoque.tab_marca (ds_descricao)
SELECT v.d FROM (VALUES ('Nike'), ('Adidas')) AS v(d)
WHERE NOT EXISTS (SELECT 1 FROM apl_estoque.tab_marca m WHERE lower(m.ds_descricao) = lower(v.d));

INSERT INTO apl_estoque.tab_modelo (ds_descricao, id_marca)
SELECT v.modelo, (SELECT id FROM apl_estoque.tab_marca WHERE ds_descricao = v.marca ORDER BY id LIMIT 1)
FROM (VALUES ('Dri-FIT', 'Nike'), ('AEROREADY', 'Adidas'), ('UltraGear 27', 'LG')) AS v(modelo, marca)
WHERE NOT EXISTS (SELECT 1 FROM apl_estoque.tab_modelo mo WHERE lower(mo.ds_descricao) = lower(v.modelo));

-- 2) Corrige produtos de vestuario que ficaram com marca/modelo de eletronicos
UPDATE apl_estoque.tab_item i SET
    id_marca  = (SELECT id FROM apl_estoque.tab_marca  WHERE ds_descricao = 'Nike'    ORDER BY id LIMIT 1),
    id_modelo = (SELECT id FROM apl_estoque.tab_modelo WHERE ds_descricao = 'Dri-FIT' ORDER BY id LIMIT 1),
    id_categoria = COALESCE((SELECT id FROM apl_estoque.tab_categoria WHERE ds_descricao = 'Roupas' ORDER BY id LIMIT 1), i.id_categoria)
WHERE (lower(i.nome) LIKE 'camiseta%' OR lower(i.nome) LIKE 'camisa%' OR lower(i.nome) LIKE 'bermuda%')
  AND (i.id_marca IS NULL OR i.id_marca IN (SELECT id FROM apl_estoque.tab_marca
                                            WHERE ds_descricao IN ('Samsung', 'Apple', 'Dell', 'Lenovo', 'LG')));

-- Modelo sempre da mesma marca do produto (quando nao for, limpa o modelo)
UPDATE apl_estoque.tab_item i SET id_modelo = NULL
WHERE i.id_modelo IS NOT NULL AND i.id_marca IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM apl_estoque.tab_modelo mo WHERE mo.id = i.id_modelo AND mo.id_marca = i.id_marca);

-- 3) Banco novo (sem produtos): cria produtos de exemplo coerentes
INSERT INTO apl_estoque.tab_item (nome, id_unidade, id_categoria, id_marca, id_modelo, codigo_barras, descricao,
       quantidade_estoque_minimo, quantidade_estoque_maximo, quantidade_estoque_atual, fl_ativo,
       vlr_custo, vlr_margem_lucro, vlr_venda, vlr_preco_antigo, nr_max_parcelas, fl_full, fl_frete_gratis,
       vlr_rating, qtd_reviews, qtd_vendidos)
SELECT v.nome,
       (SELECT id FROM apl_estoque.tab_unidade   WHERE ds_descricao = v.un  ORDER BY id LIMIT 1),
       (SELECT id FROM apl_estoque.tab_categoria WHERE ds_descricao = v.cat ORDER BY id LIMIT 1),
       (SELECT id FROM apl_estoque.tab_marca     WHERE ds_descricao = v.mar ORDER BY id LIMIT 1),
       (SELECT id FROM apl_estoque.tab_modelo    WHERE ds_descricao = v.mo  ORDER BY id LIMIT 1),
       v.ean, v.descr, v.emin, v.emax, v.eatu, TRUE, v.custo, v.margem, v.venda, v.antigo, v.parc, v.full_, v.frete,
       v.rating, v.reviews, v.vendidos
FROM (VALUES
    ('Galaxy S24 Ultra 256GB', 'UN', 'Smartphones', 'Samsung', 'Galaxy S24 Ultra', '7892509123456',
     'Smartphone com tela de 6,8", 256GB e camera de 200MP.', 2, 30, 12, 5200.00, 40.00, 7299.00, 8499.00, 12, TRUE, TRUE, 4.80, 1250, 3400),
    ('Galaxy Tab S9 128GB', 'UN', 'Tablets', 'Samsung', 'Galaxy Tab S9', '7892509123457',
     'Tablet de 11" com S Pen inclusa.', 2, 20, 7, 3900.00, 35.00, 5299.00, NULL, 10, FALSE, TRUE, 4.60, 410, 980),
    ('Notebook Inspiron 15 i5', 'UN', 'Notebooks', 'Dell', 'Inspiron 15', '7892509123458',
     'Intel Core i5, 16GB RAM, SSD 512GB, tela 15,6".', 1, 15, 5, 3100.00, 30.00, 4049.00, 4599.00, 10, FALSE, TRUE, 4.50, 320, 760),
    ('Notebook IdeaPad 3 Ryzen 5', 'UN', 'Notebooks', 'Lenovo', 'IdeaPad 3', '7892509123459',
     'AMD Ryzen 5, 8GB RAM, SSD 256GB.', 1, 15, 3, 2400.00, 30.00, 3129.00, NULL, 10, FALSE, FALSE, 4.30, 210, 540),
    ('Camiseta Dry Fit Esportiva', 'PC', 'Roupas', 'Nike', 'Dri-FIT', '7892509123460',
     'Camiseta leve de secagem rapida para treinos.', 10, 200, 80, 45.00, 120.00, 99.90, 129.90, 3, TRUE, FALSE, 4.70, 860, 5100)
) AS v(nome, un, cat, mar, mo, ean, descr, emin, emax, eatu, custo, margem, venda, antigo, parc, full_, frete, rating, reviews, vendidos)
WHERE NOT EXISTS (SELECT 1 FROM apl_estoque.tab_item);
