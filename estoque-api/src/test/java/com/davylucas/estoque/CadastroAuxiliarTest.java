package com.davylucas.estoque;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CadastroAuxiliarTest extends IntegracaoBase {

	private int inserir(String recurso, Object corpo) throws Exception {
		String r = mvc.perform(post("/" + recurso + "/inserir").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return ler(r).get("id").asInt();
	}

	private int inserirItem(Object corpo) throws Exception {
		String r = mvc.perform(post("/item/inserir").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return ler(r).get("id").asInt();
	}

	@Test
	void marcaEmUsoRetorna409ComContagemELiberadaPodeSerExcluida() throws Exception {
		int marca = inserir("marca", mapa("descricao", "Marca IT", "ativo", true));
		int modelo = inserir("modelo", mapa("descricao", "Modelo IT", "ativo", true, "marca", mapa("id", marca)));
		int item = inserirItem(mapa("nome", "Item IT", "ativo", true, "idMarca", marca, "idModelo", modelo));

		mvc.perform(delete("/marca/deletar/" + marca).header("Authorization", bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("Marca em uso por 1 produto(s) e 1 modelo(s)")));
		mvc.perform(delete("/modelo/deletar/" + modelo).header("Authorization", bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("Modelo em uso por 1 produto(s)")));

		mvc.perform(delete("/item/deletar/" + item).header("Authorization", bearer())).andExpect(status().isOk());
		mvc.perform(delete("/modelo/deletar/" + modelo).header("Authorization", bearer())).andExpect(status().isOk());
		mvc.perform(delete("/marca/deletar/" + marca).header("Authorization", bearer())).andExpect(status().isOk());
	}

	@Test
	void corETamanhoEmUsoRetornam409() throws Exception {
		int cor = inserir("cor", mapa("descricao", "Cor IT", "ativo", true));
		int tam = inserir("tamanho", mapa("descricao", "Tam IT", "ativo", true));
		int item = inserirItem(mapa("nome", "Item Cor IT", "ativo", true, "idsCores", Arrays.asList(cor),
				"idsTamanhos", Arrays.asList(tam)));

		mvc.perform(delete("/cor/deletar/" + cor).header("Authorization", bearer()))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("Cor em uso por 1 produto(s)")));
		mvc.perform(delete("/tamanho/deletar/" + tam).header("Authorization", bearer()))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("Tamanho em uso por 1 produto(s)")));

		mvc.perform(delete("/item/deletar/" + item).header("Authorization", bearer())).andExpect(status().isOk());
		mvc.perform(delete("/cor/deletar/" + cor).header("Authorization", bearer())).andExpect(status().isOk());
	}
}
