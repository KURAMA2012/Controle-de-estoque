package com.davylucas.estoque;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

class ImagemValidacaoTest extends IntegracaoBase {

	private int id;

	@BeforeEach
	void criarItem() throws Exception {
		String r = mvc.perform(post("/item/inserir").header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(mapa("nome", "Item Imagens", "ativo", true))))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		id = ler(r).get("id").asInt();
	}

	private ResultActions enviar(String tipo, byte[] bytes) throws Exception {
		return mvc.perform(multipart("/item/anexar-imagem/" + id).file(new MockMultipartFile("arquivo", "x", tipo, bytes))
				.header("Authorization", bearer()));
	}

	@Test
	void recusaTipoNaoSuportado() throws Exception {
		enviar("text/plain", "ola".getBytes()).andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("Formato de imagem")));
	}

	@Test
	void recusaConteudoQueNaoCorrespondeAoTipo() throws Exception {
		enviar("image/png", "nao sou png de verdade".getBytes()).andExpect(status().isConflict());
	}

	@Test
	void recusaImagemAcimaDe10MB() throws Exception {
		byte[] grande = Arrays.copyOf(Imagens.png(), 10 * 1024 * 1024 + 1);
		enviar("image/png", grande).andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("tamanho")));
	}

	@Test
	void limiteDe10ImagensPorItem() throws Exception {
		byte[] png = Imagens.png();
		for (int i = 0; i < 10; i++) {
			enviar("image/png", png).andExpect(status().isOk());
		}
		enviar("image/png", png).andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("Limite de 10")));
	}
}
