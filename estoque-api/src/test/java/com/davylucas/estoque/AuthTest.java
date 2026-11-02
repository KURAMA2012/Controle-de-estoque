package com.davylucas.estoque;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthTest extends IntegracaoBase {

	@Test
	void loginValidoRetornaTokenEUsuario() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(mapa("login", LOGIN, "senha", SENHA))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", not(emptyOrNullString())))
				.andExpect(jsonPath("$.usuario.login").value(LOGIN));
	}

	@Test
	void senhaErradaRetorna401() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(mapa("login", LOGIN, "senha", "errada"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message", containsString("inv")));
	}

	@Test
	void semTokenRetorna401() throws Exception {
		mvc.perform(get("/item/listar")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenInvalidoRetorna401() throws Exception {
		mvc.perform(get("/item/listar").header("Authorization", "Bearer token.invalido.x"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meComTokenRetornaUsuario() throws Exception {
		mvc.perform(get("/auth/me").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.login").value(LOGIN));
	}

	@Test
	void healthEPublico() throws Exception {
		mvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}
}
