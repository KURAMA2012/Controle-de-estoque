package com.davylucas.estoque.resource;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Modelo;
import com.davylucas.estoque.exceptions.BusinessExceptions;
import com.davylucas.estoque.repository.MarcaRepository;
import com.davylucas.estoque.repository.ModeloRepository;

@RestController
@RequestMapping("/modelo")
public class ModeloResource extends CadastroAuxiliarResource<Modelo> {

	private final ModeloRepository modeloRepository;
	private final MarcaRepository marcaRepository;

	public ModeloResource(ModeloRepository modeloRepository, MarcaRepository marcaRepository) {
		super(modeloRepository, "Modelo");
		this.modeloRepository = modeloRepository;
		this.marcaRepository = marcaRepository;
	}

	@GetMapping("listar-por-marca/{idMarca}")
	public List<Modelo> listarPorMarca(@PathVariable("idMarca") Integer idMarca) {
		return modeloRepository.findByMarcaIdOrderByDescricaoAsc(idMarca);
	}

	/** Aceita {"marca": {"id": 1}} ou "marca": null. */
	@Override
	protected void prepararParaSalvar(Modelo modelo) {
		if (modelo.getMarca() == null || modelo.getMarca().getId() == null) {
			modelo.setMarca(null);
			return;
		}
		modelo.setMarca(marcaRepository.findById(modelo.getMarca().getId())
				.orElseThrow(() -> new BusinessExceptions("Marca do modelo n\u00e3o localizada.")));
	}
}