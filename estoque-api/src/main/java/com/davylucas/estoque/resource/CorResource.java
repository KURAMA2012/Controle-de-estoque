package com.davylucas.estoque.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Cor;
import com.davylucas.estoque.repository.CorRepository;

@RestController
@RequestMapping("/cor")
public class CorResource extends CadastroAuxiliarResource<Cor> {

	public CorResource(CorRepository repository) {
		super(repository, "Cor");
	}
}