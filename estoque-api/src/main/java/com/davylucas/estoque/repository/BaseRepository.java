package com.davylucas.estoque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Metodos com a mesma semantica do antigo CrudRepositoryBean (buscar/inserir/atualizar/deletar).
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Integer> {

	default T buscar(Integer id) {
		if (id == null) {
			throw new RuntimeException("Erro a realizar consulta, id n\u00e3o informado");
		}
		return findById(id).orElse(null);
	}

	default T inserir(T entity) {
		return save(entity);
	}

	default T atualizar(T entity) {
		return save(entity);
	}

	default Boolean deletar(Integer id) {
		T entity = buscar(id);
		if (entity == null) {
			throw new RuntimeException("Erro, registro n\u00e3o localizado");
		}
		delete(entity);
		return true;
	}
}