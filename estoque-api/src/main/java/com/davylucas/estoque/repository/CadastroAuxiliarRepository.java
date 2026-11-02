package com.davylucas.estoque.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import com.davylucas.estoque.domain.CadastroAuxiliar;

@NoRepositoryBean
public interface CadastroAuxiliarRepository<T extends CadastroAuxiliar> extends BaseRepository<T> {

	List<T> findAllByOrderByDescricaoAsc();

	List<T> findByAtivoTrueOrderByDescricaoAsc();
}