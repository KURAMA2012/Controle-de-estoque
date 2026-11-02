package com.davylucas.estoque.resource;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davylucas.estoque.domain.Usuario;
import com.davylucas.estoque.dto.LoginRequest;
import com.davylucas.estoque.dto.LoginResponse;
import com.davylucas.estoque.dto.UsuarioDTO;
import com.davylucas.estoque.repository.UsuarioRepository;
import com.davylucas.estoque.security.ErroJson;
import com.davylucas.estoque.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthResource {

	private static final String CREDENCIAIS_INVALIDAS = "Login ou senha inv\u00e1lidos.";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthResource(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("login")
	public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest request, HttpServletRequest http) {
		if (request == null || request.getLogin() == null || request.getSenha() == null) {
			return erro(http, CREDENCIAIS_INVALIDAS);
		}
		Usuario usuario = usuarioRepository.findByLogin(request.getLogin().trim()).orElse(null);
		if (usuario == null || !usuario.isAtivo() || !passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
			return erro(http, CREDENCIAIS_INVALIDAS);
		}
		Date expiraEm = jwtService.calcularExpiracao();
		String token = jwtService.gerar(usuario, expiraEm);
		return ResponseEntity.ok(new LoginResponse(token, expiraEm.toInstant().toString(), UsuarioDTO.of(usuario)));
	}

	@GetMapping("me")
	public ResponseEntity<?> me(HttpServletRequest http) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Usuario usuario = auth == null ? null : usuarioRepository.findByLogin(auth.getName()).orElse(null);
		if (usuario == null || !usuario.isAtivo()) {
			return erro(http, "Usu\u00e1rio n\u00e3o localizado ou inativo.");
		}
		return ResponseEntity.ok(UsuarioDTO.of(usuario));
	}

	private ResponseEntity<?> erro(HttpServletRequest http, String mensagem) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroJson.corpo(http, 401, mensagem));
	}
}