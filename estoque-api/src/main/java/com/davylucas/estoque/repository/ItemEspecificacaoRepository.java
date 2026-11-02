package com.davylucas.estoque.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.davylucas.estoque.domain.ItemEspecificacao;

@Repository
public interface ItemEspecificacaoRepository extends BaseRepository<ItemEspecificacao> {

	@Query("select e from ItemEspecificacao e where e.item.id = :idItem order by e.id asc")
	List<ItemEspecificacao> listarPorItem(@Param("idItem") Integer idItem);
}