package com.davylucas.estoque.resource;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davylucas.estoque.consulta.ConsultaPaginada;
import com.davylucas.estoque.consulta.RequestPage;
import com.davylucas.estoque.domain.Item;
import com.davylucas.estoque.dto.ItemEspecificacaoDTO;
import com.davylucas.estoque.dto.ItemEspecificacaoRequest;
import com.davylucas.estoque.dto.ItemImagemDTO;
import com.davylucas.estoque.dto.ItemImagemUrlRequest;
import com.davylucas.estoque.dto.ItemRequestDTO;
import com.davylucas.estoque.exceptions.BusinessExceptions;
import com.davylucas.estoque.exceptions.FrameworkExceptions;
import com.davylucas.estoque.repository.ItemRepository;
import com.davylucas.estoque.service.ItemService;

/**
 * Endpoints REST de itens: CRUD, consulta paginada, galeria de imagens e especificacoes.
 */
@RestController
@RequestMapping("/item")
public class ItemResource {

	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ConsultaPaginada consultaPaginada;

	/* ===================== Endpoints especificos do item ===================== */

	@RequestMapping(value = "inserir", method = RequestMethod.POST)
	public Item inserir(@RequestBody ItemRequestDTO request) {
		return executar(() -> itemService.inserir(request), "");
	}

	@RequestMapping(value = "{id}/atualizar", method = RequestMethod.PUT)
	public Item atualizar(@PathVariable("id") Integer id, @RequestBody ItemRequestDTO request) {
		return executar(() -> itemService.atualizar(id, request), "");
	}

	@RequestMapping(method = RequestMethod.POST, path = "anexar-imagem/{idItem}", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public Boolean anexarImagem(@PathVariable("idItem") Integer idItem, @RequestParam("arquivo") MultipartFile arquivo) {
		return executar(() -> itemService.anexarImagem(idItem, arquivo), "Erro ao anexar imagem do item: ");
	}

	/**
	 * Bytes da imagem principal com Content-Type real, ETag e cache privado (listagens usam este endpoint
	 * em vez de receber os bytes no JSON). 404 quando o item nao tem imagem.
	 */
	@GetMapping("{idItem}/imagem-principal")
	public ResponseEntity<byte[]> imagemPrincipal(@PathVariable("idItem") Integer idItem,
			@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
		byte[] bytes = executar(() -> itemService.imagemPrincipal(idItem), "Erro ao obter imagem principal: ");
		if (bytes == null || bytes.length == 0) {
			return ResponseEntity.notFound().build();
		}
		String etag = "\"" + Integer.toHexString(java.util.Arrays.hashCode(bytes)) + "\"";
		CacheControl cache = CacheControl.maxAge(7, java.util.concurrent.TimeUnit.DAYS).cachePrivate();
		if (etag.equals(ifNoneMatch)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).cacheControl(cache).build();
		}
		return ResponseEntity.ok().contentType(tipoImagem(bytes)).contentLength(bytes.length).eTag(etag)
				.cacheControl(cache).body(bytes);
	}

	private static MediaType tipoImagem(byte[] b) {
		if (b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
			return MediaType.IMAGE_PNG;
		}
		if (b.length > 2 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
			return MediaType.IMAGE_GIF;
		}
		if (b.length > 11 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E'
				&& b[10] == 'B' && b[11] == 'P') {
			return MediaType.parseMediaType("image/webp");
		}
		if (b.length > 1 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) {
			return MediaType.IMAGE_JPEG;
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	@RequestMapping(value = "{idItem}/imagens", method = RequestMethod.GET)
	public List<ItemImagemDTO> listarImagens(@PathVariable("idItem") Integer idItem) {
		return executar(() -> itemService.listarImagens(idItem), "Erro ao listar imagens do item: ");
	}

	@RequestMapping(value = "{idItem}/imagens/url", method = RequestMethod.POST)
	public Boolean adicionarImagemPorUrl(@PathVariable("idItem") Integer idItem,
			@RequestBody ItemImagemUrlRequest request) {
		return executar(() -> itemService.adicionarImagemPorUrl(idItem, request != null ? request.getUrl() : null),
				"Erro ao adicionar imagem por URL: ");
	}

	@RequestMapping(value = "imagens/{idImagem}", method = RequestMethod.DELETE)
	public Boolean removerImagem(@PathVariable("idImagem") Integer idImagem) {
		return executar(() -> itemService.removerImagem(idImagem), "Erro ao remover imagem do item: ");
	}

	@RequestMapping(value = "imagens/{idImagem}/definir-principal", method = RequestMethod.PUT)
	public Boolean definirImagemPrincipal(@PathVariable("idImagem") Integer idImagem) {
		return executar(() -> itemService.definirImagemPrincipal(idImagem), "Erro ao definir imagem principal: ");
	}

	@RequestMapping(value = "{idItem}/especificacoes", method = RequestMethod.GET)
	public List<ItemEspecificacaoDTO> listarEspecificacoes(@PathVariable("idItem") Integer idItem) {
		return executar(() -> itemService.listarEspecificacoes(idItem),
				"Erro ao listar especifica\u00e7\u00f5es do item: ");
	}

	@RequestMapping(value = "{idItem}/especificacoes", method = RequestMethod.POST)
	public ItemEspecificacaoDTO inserirEspecificacao(@PathVariable("idItem") Integer idItem,
			@RequestBody ItemEspecificacaoRequest request) {
		return executar(() -> itemService.inserirEspecificacao(idItem, request),
				"Erro ao inserir especifica\u00e7\u00e3o: ");
	}

	@RequestMapping(value = "especificacoes/{id}", method = RequestMethod.PUT)
	public ItemEspecificacaoDTO atualizarEspecificacao(@PathVariable("id") Integer id,
			@RequestBody ItemEspecificacaoRequest request) {
		return executar(() -> itemService.atualizarEspecificacao(id, request),
				"Erro ao atualizar especifica\u00e7\u00e3o: ");
	}

	@RequestMapping(value = "especificacoes/{id}", method = RequestMethod.DELETE)
	public Boolean removerEspecificacao(@PathVariable("id") Integer id) {
		return executar(() -> itemService.removerEspecificacao(id), "Erro ao remover especifica\u00e7\u00e3o: ");
	}

	/* ===================== Endpoints CRUD genericos ===================== */

	@RequestMapping(value = "inserir-entidade", method = RequestMethod.POST)
	public Item inserirEntidade(@RequestBody Item entidade) {
		try {
			return itemRepository.inserir(entidade);
		} catch (FrameworkExceptions e) {
			throw e;
		} catch (Exception e) {
			throw new FrameworkExceptions(e);
		}
	}

	@RequestMapping(value = "{id}/atualizar-entidade", method = RequestMethod.PUT)
	public Item atualizarEntidade(@PathVariable("id") Integer id, @RequestBody Item entidade) {
		Item base = itemRepository.buscar(id);
		BeanUtils.copyProperties(entidade, base, "id");
		try {
			return itemRepository.atualizar(base);
		} catch (Exception e) {
			throw new FrameworkExceptions(e);
		}
	}

	@GetMapping("detalhar/{id}")
	public ResponseEntity<Item> detalhar(@PathVariable("id") Integer id) {
		try {
			Item item = itemRepository.buscar(id);
			return item == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(item);
		} catch (Exception e) {
			throw new FrameworkExceptions(e);
		}
	}

	@RequestMapping(value = "listar", method = RequestMethod.GET)
	public List<Item> listar() {
		return itemRepository.findAll();
	}

	@GetMapping("buscar/{id}")
	public Item buscar(@PathVariable("id") Integer id) {
		return itemRepository.buscar(id);
	}

	@DeleteMapping("deletar/{id}")
	public Boolean deletar(@PathVariable("id") Integer id) {
		return executar(() -> itemService.deletar(id), "Não foi possível excluir o produto: ");
	}

	@RequestMapping(value = "consultar", method = RequestMethod.POST)
	public Page<Item> consultar(@RequestBody RequestPage requestPage) {
		return consultaPaginada.consultar(requestPage, Item.class);
	}

	/* ===================== util ===================== */

	private <R> R executar(Supplier<R> acao, String prefixoErro) {
		try {
			return acao.get();
		} catch (BusinessExceptions e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessExceptions(prefixoErro + e.getMessage(), e);
		}
	}
}