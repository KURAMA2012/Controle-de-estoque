package com.davylucas.estoque.dto;

import com.davylucas.estoque.domain.Usuario;

public class UsuarioDTO {

	private Integer id;
	private String login;
	private String nome;
	private String perfil;

	public static UsuarioDTO of(Usuario u) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.id = u.getId();
		dto.login = u.getLogin();
		dto.nome = u.getNome();
		dto.perfil = u.getPerfil();
		return dto;
	}

	public Integer getId() { return id; }
	public String getLogin() { return login; }
	public String getNome() { return nome; }
	public String getPerfil() { return perfil; }
}