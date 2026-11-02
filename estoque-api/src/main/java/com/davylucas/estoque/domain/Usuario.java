package com.davylucas.estoque.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tab_usuario", schema = "apl_estoque")
public class Usuario {

	public static final String PERFIL_ADMIN = "ADMIN";
	public static final String PERFIL_USER = "USER";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ds_login", nullable = false, unique = true, length = 100)
	private String login;

	@JsonIgnore
	@Column(name = "ds_senha", nullable = false, length = 100)
	private String senha;

	@Column(name = "ds_nome", length = 255)
	private String nome;

	@Column(name = "ds_perfil", nullable = false, length = 30)
	private String perfil = PERFIL_USER;

	@Column(name = "fl_ativo", nullable = false)
	private boolean ativo = true;

	@Column(name = "dt_criacao", nullable = false)
	private LocalDateTime dataCriacao = LocalDateTime.now();

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	public String getLogin() { return login; }
	public void setLogin(String login) { this.login = login; }
	public String getSenha() { return senha; }
	public void setSenha(String senha) { this.senha = senha; }
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public String getPerfil() { return perfil; }
	public void setPerfil(String perfil) { this.perfil = perfil; }
	public boolean isAtivo() { return ativo; }
	public void setAtivo(boolean ativo) { this.ativo = ativo; }
	public LocalDateTime getDataCriacao() { return dataCriacao; }
	public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}