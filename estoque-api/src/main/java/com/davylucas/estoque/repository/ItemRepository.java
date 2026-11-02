package com.davylucas.estoque.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.davylucas.estoque.domain.Item;

@Repository
public interface ItemRepository extends BaseRepository<Item> {

	@Transactional
	@Modifying
	@Query("update Item e set e.imagem = :arquivo where e.id = :idItem")
	void atualizarImagem(@Param("idItem") Integer idItem, @Param("arquivo") byte[] arquivo);
}