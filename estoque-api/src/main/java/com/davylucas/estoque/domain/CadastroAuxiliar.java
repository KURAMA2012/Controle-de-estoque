package com.davylucas.estoque.domain;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base dos cadastros auxiliares (categoria, marca, modelo, unidade, cor, tamanho).
 * JSON: {"id": 1, "descricao": "...", "ativo": true}
 */
@MappedSuperclass
public abstract class CadastroAuxiliar {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ds_descricao", nullable = false, length = 255)
	private String descricao;

	@Column(name = "fl_ativo", nullable = false)
	private boolean ativo = true;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CadastroAuxiliar outro = (CadastroAuxiliar) obj;
		return id != null && Objects.equals(id, outro.id);
	}
}