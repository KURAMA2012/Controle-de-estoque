package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Modelo pertence (opcionalmente) a uma Marca. Marca nao referencia Modelo, entao nao ha recursao no JSON:
 * {"id": 1, "descricao": "Galaxy S24 Ultra", "ativo": true, "marca": {"id": 1, "descricao": "Samsung", "ativo": true}}
 */
@Entity
@Table(name = "tab_modelo", schema = "apl_estoque")
public class Modelo extends CadastroAuxiliar {

	@ManyToOne
	@JoinColumn(name = "id_marca")
	private Marca marca;

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}
}