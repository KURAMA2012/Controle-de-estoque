package com.davylucas.estoque.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.davylucas.estoque.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtService {

	private static final int TAMANHO_MINIMO_SEGREDO = 64;

	private final byte[] chave;
	private final long expiracaoMs;

	public JwtService(@Value("${jwt.secret:}") String segredo, @Value("${jwt.expiration:86400000}") long expiracaoMs) {
		if (segredo == null || segredo.trim().isEmpty()) {
			throw new IllegalStateException(
					"JWT_SECRET nao definido. Defina a variavel de ambiente JWT_SECRET com pelo menos 64 bytes (HS512).");
		}
		byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
			throw new IllegalStateException("JWT_SECRET muito curto (" + bytes.length
					+ " bytes). HS512 exige pelo menos 64 bytes.");
		}
		if (expiracaoMs <= 0) {
			throw new IllegalStateException("JWT_EXPIRATION deve ser maior que zero (milissegundos).");
		}
		this.chave = bytes;
		this.expiracaoMs = expiracaoMs;
	}

	public Date calcularExpiracao() {
		return new Date(System.currentTimeMillis() + expiracaoMs);
	}

	public String gerar(Usuario usuario, Date expiraEm) {
		return Jwts.builder()
				.setSubject(usuario.getLogin())
				.claim("id", usuario.getId())
				.claim("perfil", usuario.getPerfil())
				.setIssuedAt(new Date())
				.setExpiration(expiraEm)
				.signWith(SignatureAlgorithm.HS512, chave)
				.compact();
	}

	/** Lanca JwtException/IllegalArgumentException se o token for invalido ou expirado. */
	public Claims validar(String token) {
		return Jwts.parser().setSigningKey(chave).parseClaimsJws(token).getBody();
	}
}