package com.davylucas.estoque.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.davylucas.estoque.domain.Usuario;
import com.davylucas.estoque.repository.UsuarioRepository;

/** Cria o primeiro ADMIN a partir de APP_ADMIN_LOGIN/APP_ADMIN_SENHA, somente se nao houver usuarios. */
@Component
public class AdminBootstrap implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(AdminBootstrap.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.login:}")
	private String login;

	@Value("${app.admin.senha:}")
	private String senha;

	public AdminBootstrap(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.count() > 0) {
			return;
		}
		if (login == null || login.trim().isEmpty() || senha == null || senha.isEmpty()) {
			LOG.warn("Nenhum usuario cadastrado e APP_ADMIN_LOGIN/APP_ADMIN_SENHA nao definidos: "
					+ "admin inicial NAO foi criado; ninguem conseguira fazer login.");
			return;
		}
		Usuario admin = new Usuario();
		admin.setLogin(login.trim());
		admin.setSenha(passwordEncoder.encode(senha));
		admin.setNome("Administrador");
		admin.setPerfil(Usuario.PERFIL_ADMIN);
		usuarioRepository.save(admin);
		LOG.info("Usuario admin inicial '{}' criado a partir das variaveis de ambiente.", admin.getLogin());
	}
}