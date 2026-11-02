package com.davylucas.estoque.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Marca;
import com.davylucas.estoque.repository.MarcaRepository;

@RestController
@RequestMapping("/marca")
public class MarcaResource extends CadastroAuxiliarResource<Marca> {

	public MarcaResource(MarcaRepository repository) {
		super(repository, "Marca");
	}
}