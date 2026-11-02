package com.davylucas.estoque;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base dos testes de integracao: sobe um Postgres real (Testcontainers) uma unica vez,
 * aplica as migrations do Flyway e cria o admin de teste via AdminBootstrap.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegracaoBase {

	protected static final String LOGIN = "admin-teste";
	protected static final String SENHA = "senha-de-teste-123";

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("estoque");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void propriedades(DynamicPropertyRegistry r) {
		r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		r.add("spring.datasource.username", POSTGRES::getUsername);
		r.add("spring.datasource.password", POSTGRES::getPassword);
		r.add("jwt.secret", () -> "segredo-de-teste-com-mais-de-sessenta-e-quatro-bytes-para-hs512-0123456789");
		r.add("app.admin.login", () -> LOGIN);
		r.add("app.admin.senha", () -> SENHA);
		r.add("logging.file.name", () -> "");
	}

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected ObjectMapper json;

	@Autowired
	protected JdbcTemplate jdbc;

	private static String token;

	protected String token() throws Exception {
		if (token == null) {
			String corpo = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
					.content(json.writeValueAsString(mapa("login", LOGIN, "senha", SENHA))))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			token = json.readTree(corpo).get("token").asText();
		}
		return token;
	}

	protected String bearer() throws Exception {
		return "Bearer " + token();
	}

	protected JsonNode ler(String corpo) throws Exception {
		return json.readTree(corpo);
	}

	protected int idDe(String tabela, String descricao) {
		return jdbc.queryForObject("select id from apl_estoque." + tabela + " where ds_descricao = ? order by id limit 1",
				Integer.class, descricao);
	}

	protected int contar(String sql, Object... args) {
		return jdbc.queryForObject(sql, Integer.class, args);
	}

	protected static Map<String, Object> mapa(Object... kv) {
		Map<String, Object> m = new java.util.LinkedHashMap<>();
		for (int i = 0; i < kv.length; i += 2) {
			m.put((String) kv[i], kv[i + 1]);
		}
		return m;
	}
}
