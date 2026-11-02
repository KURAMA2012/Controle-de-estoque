package com.davylucas.estoque.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Tamanho;
import com.davylucas.estoque.repository.TamanhoRepository;

@RestController
@RequestMapping("/tamanho")
public class TamanhoResource extends CadastroAuxiliarResource<Tamanho> {

	public TamanhoResource(TamanhoRepository repository) {
		super(repository, "Tamanho");
	}
}