package com.davylucas.estoque.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Categoria;
import com.davylucas.estoque.repository.CategoriaRepository;

@RestController
@RequestMapping("/categoria")
public class CategoriaResource extends CadastroAuxiliarResource<Categoria> {

	public CategoriaResource(CategoriaRepository repository) {
		super(repository, "Categoria");
	}
}