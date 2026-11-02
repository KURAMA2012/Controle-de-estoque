package com.davylucas.estoque.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.davylucas.estoque.domain.ItemImagem;

@Repository
public interface ItemImagemRepository extends BaseRepository<ItemImagem> {

	@Query("select i from ItemImagem i where i.item.id = :idItem order by i.ordem asc, i.id asc")
	List<ItemImagem> listarPorItem(@Param("idItem") Integer idItem);

	@Query("select max(i.ordem) from ItemImagem i where i.item.id = :idItem")
	Integer maxOrdemOuNulo(@Param("idItem") Integer idItem);

	default Integer maxOrdem(Integer idItem) {
		Integer max = maxOrdemOuNulo(idItem);
		return max == null ? 0 : max;
	}

	ItemImagem findFirstByItemIdOrderByOrdemDescIdDesc(Integer idItem);

	default ItemImagem buscarUltimaPorItem(Integer idItem) {
		return findFirstByItemIdOrderByOrdemDescIdDesc(idItem);
	}
}