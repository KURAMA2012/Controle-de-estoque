package com.davylucas.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class ItemCrudTest extends IntegracaoBase {

	int criarItem(String nome, Map<String, Object> extras) throws Exception {
		Map<String, Object> corpo = mapa("nome", nome, "ativo", true, "valorVenda", 199.90, "quantidadeEstoqueAtual", 5);
		corpo.putAll(extras);
		String r = mvc.perform(post("/item/inserir").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return ler(r).get("id").asInt();
	}

	@Test
	void crudCompletoComExclusaoEmCascata() throws Exception {
		int cor = idDe("tab_cor", "Preto");
		int tamanho = idDe("tab_tamanho", "M");
		int id = criarItem("Camiseta Teste CRUD", mapa("idsCores", Arrays.asList(cor), "idsTamanhos", Arrays.asList(tamanho),
				"idCategoria", idDe("tab_categoria", "Roupas")));

		mvc.perform(get("/item/detalhar/" + id).header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Camiseta Teste CRUD"))
				.andExpect(jsonPath("$.cores[0].id").value(cor))
				.andExpect(jsonPath("$.tamanhos[0].id").value(tamanho));

		mvc.perform(put("/item/" + id + "/atualizar").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(mapa("nome", "Camiseta Teste CRUD Editada", "ativo", true,
						"valorVenda", 149.90, "idsCores", Arrays.asList(cor), "idsTamanhos", Arrays.asList(tamanho)))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Camiseta Teste CRUD Editada"));

		mvc.perform(post("/item/" + id + "/especificacoes").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(mapa("chave", "Tecido", "valor", "Poliester"))))
				.andExpect(status().isOk());
		mvc.perform(multipart("/item/anexar-imagem/" + id)
				.file(new MockMultipartFile("arquivo", "a.png", "image/png", Imagens.png()))
				.header("Authorization", bearer()))
				.andExpect(status().isOk());

		assertThat(contar("select count(*) from apl_estoque.tab_item_imagem where id_item = ?", id)).isEqualTo(1);
		assertThat(contar("select count(*) from apl_estoque.tab_item_especificacao where id_item = ?", id)).isEqualTo(1);

		mvc.perform(delete("/item/deletar/" + id).header("Authorization", bearer()))
				.andExpect(status().isOk()).andExpect(content().string("true"));

		mvc.perform(get("/item/detalhar/" + id).header("Authorization", bearer())).andExpect(status().isNotFound());
		for (String t : new String[] { "tab_item_imagem", "tab_item_especificacao", "tab_item_cor", "tab_item_tamanho" }) {
			assertThat(contar("select count(*) from apl_estoque." + t + " where id_item = ?", id)).as(t).isZero();
		}
	}

	@Test
	void excluirItemInexistenteRetorna409ComMensagem() throws Exception {
		mvc.perform(delete("/item/deletar/999999").header("Authorization", bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("localizado")));
	}

	@Test
	void dadosDeExemploSaoCoerentes() throws Exception {
		// V1_0_0_5: banco novo recebe produtos de exemplo; camiseta com marca de vestuario
		assertThat(contar("select count(*) from apl_estoque.tab_item i join apl_estoque.tab_marca m on m.id = i.id_marca "
				+ "where i.nome like 'Camiseta Dry Fit%' and m.ds_descricao = 'Nike'")).isEqualTo(1);
		assertThat(contar("select count(*) from apl_estoque.tab_item i join apl_estoque.tab_modelo mo on mo.id = i.id_modelo "
				+ "where mo.id_marca <> i.id_marca")).isZero();
	}
}
