package com.davylucas.estoque.dto;

public class LoginResponse {

	private final String token;
	private final String tipo = "Bearer";
	/** Instante de expiracao em ISO-8601 UTC, ex.: 2026-11-02T09:30:00Z */
	private final String expiraEm;
	private final UsuarioDTO usuario;

	public LoginResponse(String token, String expiraEm, UsuarioDTO usuario) {
		this.token = token;
		this.expiraEm = expiraEm;
		this.usuario = usuario;
	}

	public String getToken() { return token; }
	public String getTipo() { return tipo; }
	public String getExpiraEm() { return expiraEm; }
	public UsuarioDTO getUsuario() { return usuario; }
}