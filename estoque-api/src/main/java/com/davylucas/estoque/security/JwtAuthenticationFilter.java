package com.davylucas.estoque.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/** Le "Authorization: Bearer <token>"; se valido, autentica com ROLE_<perfil>. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String ATRIBUTO_ERRO = "estoque.jwt.erro";
	private static final String PREFIXO = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith(PREFIXO)) {
			try {
				Claims claims = jwtService.validar(header.substring(PREFIXO.length()).trim());
				String perfil = claims.get("perfil", String.class);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						claims.getSubject(), null, perfil == null ? Collections.emptyList()
								: Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + perfil)));
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
				request.setAttribute(ATRIBUTO_ERRO, "Token inv\u00e1lido ou expirado.");
			}
		}
		chain.doFilter(request, response);
	}
}