package com.davylucas.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

/** POST /item/consultar sobre os produtos de exemplo criados pela migration V1_0_0_5. */
class ConsultaTest extends IntegracaoBase {

	private JsonNode consultar(Object corpo) throws Exception {
		String r = mvc.perform(post("/item/consultar").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return ler(r);
	}

	@Test
	void filtraPorNomeSemDiferenciarMaiusculas() throws Exception {
		JsonNode p = consultar(mapa("page", 0, "size", 20, "filtros", mapa("nome", "notebook")));
		assertThat(p.get("totalElements").asInt()).isEqualTo(2);
		p.get("content").forEach(i -> assertThat(i.get("nome").asText()).containsIgnoringCase("notebook"));
	}

	@Test
	void filtraPorMarcaAssociada() throws Exception {
		int samsung = idDe("tab_marca", "Samsung");
		JsonNode p = consultar(mapa("page", 0, "size", 20, "filtros", mapa("idMarca", mapa("id", samsung))));
		assertThat(p.get("totalElements").asInt()).isEqualTo(2);
		p.get("content").forEach(i -> assertThat(i.get("idMarca").get("id").asInt()).isEqualTo(samsung));
	}

	@Test
	void ordenaPorValorDeVendaDecrescenteEPagina() throws Exception {
		JsonNode todos = consultar(mapa("page", 0, "size", 2, "orderBy", "valorVenda,desc",
				"filtros", mapa("nome", "notebook")));
		List<Double> valores = new ArrayList<>();
		todos.get("content").forEach(i -> valores.add(i.get("valorVenda").asDouble()));
		assertThat(valores).containsExactly(4049.00, 3129.00);

		JsonNode pagina2 = consultar(mapa("page", 1, "size", 1, "orderBy", "-valorVenda",
				"filtros", mapa("nome", "notebook")));
		assertThat(pagina2.get("content")).hasSize(1);
		assertThat(pagina2.get("content").get(0).get("valorVenda").asDouble()).isEqualTo(3129.00);
		assertThat(pagina2.get("totalElements").asInt()).isEqualTo(2);

		JsonNode asc = consultar(mapa("page", 0, "size", 1, "orderBy", "valorVenda", "direcao", "asc",
				"filtros", mapa("nome", "Camiseta Dry")));
		assertThat(asc.get("content").get(0).get("nome").asText()).isEqualTo("Camiseta Dry Fit Esportiva");
	}
}
