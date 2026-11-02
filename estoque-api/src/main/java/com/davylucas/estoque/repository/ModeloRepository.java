package com.davylucas.estoque.repository;

import com.davylucas.estoque.domain.Modelo;

public interface ModeloRepository extends CadastroAuxiliarRepository<Modelo> {

	java.util.List<Modelo> findByMarcaIdOrderByDescricaoAsc(Integer idMarca);
}