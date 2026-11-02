package com.davylucas.estoque.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Unidade;
import com.davylucas.estoque.repository.UnidadeRepository;

@RestController
@RequestMapping("/unidade")
public class UnidadeResource extends CadastroAuxiliarResource<Unidade> {

	public UnidadeResource(UnidadeRepository repository) {
		super(repository, "Unidade");
	}
}