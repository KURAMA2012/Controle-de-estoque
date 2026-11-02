package com.davylucas.estoque.repository;

import java.util.Optional;

import com.davylucas.estoque.domain.Usuario;

public interface UsuarioRepository extends BaseRepository<Usuario> {

	Optional<Usuario> findByLogin(String login);
}