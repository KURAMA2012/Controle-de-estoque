package com.davylucas.estoque.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.davylucas.estoque.exceptions.BusinessExceptions;
import com.davylucas.estoque.util.Texto;
import com.davylucas.estoque.domain.Cor;
import com.davylucas.estoque.domain.Modelo;
import com.davylucas.estoque.domain.Tamanho;
import com.davylucas.estoque.domain.Item;
import com.davylucas.estoque.domain.ItemEspecificacao;
import com.davylucas.estoque.domain.ItemImagem;
import com.davylucas.estoque.dto.ItemEspecificacaoDTO;
import com.davylucas.estoque.dto.ItemEspecificacaoRequest;
import com.davylucas.estoque.dto.ItemImagemDTO;
import com.davylucas.estoque.dto.ItemRequestDTO;
import com.davylucas.estoque.repository.BaseRepository;
import com.davylucas.estoque.repository.CategoriaRepository;
import com.davylucas.estoque.repository.CorRepository;
import com.davylucas.estoque.repository.ItemEspecificacaoRepository;
import com.davylucas.estoque.repository.ItemImagemRepository;
import com.davylucas.estoque.repository.ItemRepository;
import com.davylucas.estoque.repository.MarcaRepository;
import com.davylucas.estoque.repository.ModeloRepository;
import com.davylucas.estoque.repository.TamanhoRepository;
import com.davylucas.estoque.repository.UnidadeRepository;

@Service
public class ItemService {

	private static final int LIMITE_IMAGENS = 10;
	private static final int TAMANHO_MAXIMO_URL = 10 * 1024 * 1024;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ItemImagemRepository itemImagemRepository;

	@Autowired
	private ItemEspecificacaoRepository itemEspecificacaoRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private ModeloRepository modeloRepository;

	@Autowired
	private CorRepository corRepository;

	@Autowired
	private TamanhoRepository tamanhoRepository;

	@Value("${estoque.imagem.hosts-permitidos:}")
	private String hostsPermitidosImagem;

	@Transactional
	public Item inserir(ItemRequestDTO request) {
		validar(request);
		Item item = toItem(request, null);
		return itemRepository.inserir(item);
	}

	@Transactional
	public Item atualizar(Integer id, ItemRequestDTO request) {
		if (id == null) {
			throw new BusinessExceptions("Id do item é obrigatório.");
		}

		Item existente = itemRepository.buscar(id);
		if (existente == null) {
			throw new BusinessExceptions("Item não localizado.");
		}

		validar(request);
		Item item = toItem(request, id);
		// Foto so muda via anexarImagem; o payload do front nao envia imagem.
		item.setImagem(existente.getImagem());
		// Galeria e especificacoes sao gerenciadas a parte.
		item.setImagens(existente.getImagens());
		item.setEspecificacoes(existente.getEspecificacoes());
		return itemRepository.atualizar(item);
	}

	/** Bytes da imagem principal (sem carregar o Item inteiro). Null quando nao ha imagem. */
	@Transactional(readOnly = true)
	public byte[] imagemPrincipal(Integer idItem) {
		if (idItem == null) {
			throw new BusinessExceptions("Id do item é obrigatório.");
		}
		List<byte[]> r = em.createQuery("select i.imagem from Item i where i.id = :id", byte[].class)
				.setParameter("id", idItem).getResultList();
		return r.isEmpty() ? null : r.get(0);
	}

	@Transactional(readOnly = true)
	public List<ItemImagemDTO> listarImagens(Integer idItem) {
		Item item = validarItemExistente(idItem);
		List<ItemImagem> imagens = itemImagemRepository.listarPorItem(idItem);
		List<ItemImagemDTO> resultado = new ArrayList<>();
		for (ItemImagem imagem : imagens) {
			resultado.add(toImagemDTO(imagem, idItem, item.getImagem()));
		}
		return resultado;
	}

	@Transactional
	public Boolean anexarImagem(Integer idItem, MultipartFile arquivo) {
		if (idItem == null) {
			throw new BusinessExceptions("Id do item é obrigatório.");
		}
		if (arquivo == null || arquivo.isEmpty()) {
			throw new BusinessExceptions("Arquivo da imagem é obrigatório.");
		}

		Item existente = itemRepository.buscar(idItem);
		if (existente == null) {
			throw new BusinessExceptions("Item não localizado.");
		}
		validarLimiteImagens(idItem);

		String contentType = validarTipoImagem(arquivo.getContentType());
		if (arquivo.getSize() > TAMANHO_MAXIMO_URL) {
			throw new BusinessExceptions("A imagem excede o tamanho máximo permitido.");
		}

		try {
			byte[] bytes = arquivo.getBytes();
			validarTamanhoImagem(bytes);
			validarBytesImagem(bytes, contentType);
			Integer proximaOrdem = itemImagemRepository.maxOrdem(idItem) + 1;

			ItemImagem galeria = new ItemImagem();
			galeria.setItem(existente);
			galeria.setImagem(bytes);
			galeria.setUrl(null);
			galeria.setOrdem(proximaOrdem);
			itemImagemRepository.inserir(galeria);

			if (existente.getImagem() == null || existente.getImagem().length == 0) {
				itemRepository.atualizarImagem(idItem, bytes);
			}
			return Boolean.TRUE;
		} catch (BusinessExceptions e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessExceptions("Erro ao anexar imagem do item: " + e.getMessage(), e);
		}
	}

	@Transactional
	public Boolean adicionarImagemPorUrl(Integer idItem, String endereco) {
		if (idItem == null) {
			throw new BusinessExceptions("Id do item é obrigatório.");
		}
		if (endereco == null || endereco.trim().isEmpty()) {
			throw new BusinessExceptions("URL da imagem é obrigatória.");
		}

		Item existente = itemRepository.buscar(idItem);
		if (existente == null) {
			throw new BusinessExceptions("Item não localizado.");
		}
		validarLimiteImagens(idItem);

		byte[] bytes = baixarImagem(endereco.trim());
		try {
			Integer proximaOrdem = itemImagemRepository.maxOrdem(idItem) + 1;

			ItemImagem galeria = new ItemImagem();
			galeria.setItem(existente);
			galeria.setImagem(bytes);
			galeria.setUrl(endereco.trim());
			galeria.setOrdem(proximaOrdem);
			itemImagemRepository.inserir(galeria);

			if (existente.getImagem() == null || existente.getImagem().length == 0) {
				itemRepository.atualizarImagem(idItem, bytes);
			}
			return Boolean.TRUE;
		} catch (BusinessExceptions e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessExceptions("Erro ao adicionar imagem por URL: " + e.getMessage(), e);
		}
	}

	@Transactional
	public Boolean removerImagem(Integer idImagem) {
		if (idImagem == null) {
			throw new BusinessExceptions("Id da imagem é obrigatório.");
		}

		ItemImagem imagem = itemImagemRepository.buscar(idImagem);
		if (imagem == null) {
			throw new BusinessExceptions("Imagem não localizada.");
		}

		Integer idItem = imagem.getItem() != null ? imagem.getItem().getId() : null;
		if (idItem == null) {
			throw new BusinessExceptions("Item da imagem não localizado.");
		}

		Item existente = validarItemExistente(idItem);
		boolean eraPrincipal = Arrays.equals(existente.getImagem(), imagem.getImagem());
		itemImagemRepository.deletar(idImagem);

		if (eraPrincipal) {
			itemRepository.atualizarImagem(idItem, null);
		}
		return Boolean.TRUE;
	}

	@Transactional
	public Boolean definirImagemPrincipal(Integer idImagem) {
		if (idImagem == null) {
			throw new BusinessExceptions("Id da imagem é obrigatório.");
		}

		ItemImagem imagem = itemImagemRepository.buscar(idImagem);
		if (imagem == null) {
			throw new BusinessExceptions("Imagem não localizada.");
		}

		Integer idItem = imagem.getItem() != null ? imagem.getItem().getId() : null;
		if (idItem == null) {
			throw new BusinessExceptions("Item da imagem não localizado.");
		}
		validarItemExistente(idItem);

		byte[] bytes = imagem.getImagem();
		itemRepository.atualizarImagem(idItem, bytes == null ? null : Arrays.copyOf(bytes, bytes.length));
		return Boolean.TRUE;
	}

	@Transactional(readOnly = true)
	public List<ItemEspecificacaoDTO> listarEspecificacoes(Integer idItem) {
		validarItemExistente(idItem);
		List<ItemEspecificacao> lista = itemEspecificacaoRepository.listarPorItem(idItem);
		List<ItemEspecificacaoDTO> resultado = new ArrayList<>();
		for (ItemEspecificacao espec : lista) {
			resultado.add(toEspecificacaoDTO(espec, idItem));
		}
		return resultado;
	}

	@Transactional
	public ItemEspecificacaoDTO inserirEspecificacao(Integer idItem, ItemEspecificacaoRequest request) {
		Item item = validarItemExistente(idItem);
		validarEspecificacao(request);

		ItemEspecificacao espec = new ItemEspecificacao();
		espec.setItem(item);
		espec.setChave(request.getChave().trim());
		espec.setValor(request.getValor() != null ? request.getValor().trim() : null);
		ItemEspecificacao salva = itemEspecificacaoRepository.inserir(espec);
		return toEspecificacaoDTO(salva, idItem);
	}

	@Transactional
	public ItemEspecificacaoDTO atualizarEspecificacao(Integer id, ItemEspecificacaoRequest request) {
		if (id == null) {
			throw new BusinessExceptions("Id da especificação é obrigatório.");
		}
		validarEspecificacao(request);

		ItemEspecificacao existente = itemEspecificacaoRepository.buscar(id);
		if (existente == null) {
			throw new BusinessExceptions("Especificação não localizada.");
		}

		existente.setChave(request.getChave().trim());
		existente.setValor(request.getValor() != null ? request.getValor().trim() : null);
		ItemEspecificacao atualizada = itemEspecificacaoRepository.atualizar(existente);
		Integer idItem = atualizada.getItem() != null ? atualizada.getItem().getId() : null;
		return toEspecificacaoDTO(atualizada, idItem);
	}

	@Transactional
	public Boolean removerEspecificacao(Integer id) {
		if (id == null) {
			throw new BusinessExceptions("Id da especificação é obrigatório.");
		}
		ItemEspecificacao existente = itemEspecificacaoRepository.buscar(id);
		if (existente == null) {
			throw new BusinessExceptions("Especificação não localizada.");
		}
		return itemEspecificacaoRepository.deletar(id);
	}

	private void validarLimiteImagens(Integer idItem) {
		if (itemImagemRepository.listarPorItem(idItem).size() >= LIMITE_IMAGENS) {
			throw new BusinessExceptions("Limite de 10 imagens por item");
		}
	}

	private byte[] baixarImagem(String endereco) {
		URI atual = criarUriImagem(endereco);
		for (int redirecionamentos = 0; redirecionamentos <= 5; redirecionamentos++) {
			validarUriImagem(atual);
			HttpURLConnection conexao = null;
			try {
				// A validacao ocorre antes da conexao; DNS pode mudar entre as duas operacoes.
				URL url = atual.toURL();
				conexao = (HttpURLConnection) url.openConnection();
				conexao.setConnectTimeout(10000);
				conexao.setReadTimeout(20000);
				conexao.setInstanceFollowRedirects(false);
				conexao.setRequestProperty("User-Agent", "estoque-api-imagem/1.0");

				int status = conexao.getResponseCode();
				if (ehRedirecionamento(status)) {
					if (redirecionamentos == 5) {
						throw new BusinessExceptions("Quantidade máxima de redirecionamentos excedida.");
					}
					String localizacao = conexao.getHeaderField("Location");
					if (localizacao == null || localizacao.trim().isEmpty()) {
						throw new BusinessExceptions("Redirecionamento da imagem sem destino.");
					}
					try {
						atual = atual.resolve(localizacao.trim());
					} catch (Exception e) {
						throw new BusinessExceptions("Destino de redirecionamento da imagem inválido.", e);
					}
					continue;
				}
				if (status < 200 || status >= 300) {
					throw new BusinessExceptions("Não foi possível acessar a URL da imagem.");
				}

				String contentType = validarTipoImagem(conexao.getContentType());
				int tamanho = conexao.getContentLength();
				if (tamanho > TAMANHO_MAXIMO_URL) {
					throw new BusinessExceptions("A imagem da URL excede o tamanho máximo permitido.");
				}

				try (InputStream entrada = conexao.getInputStream(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
					byte[] buffer = new byte[8192];
					int lidos;
					while ((lidos = entrada.read(buffer)) != -1) {
						if (saida.size() + lidos > TAMANHO_MAXIMO_URL) {
							throw new BusinessExceptions("A imagem da URL excede o tamanho máximo permitido.");
						}
						saida.write(buffer, 0, lidos);
					}
					byte[] bytes = saida.toByteArray();
					validarBytesImagem(bytes, contentType);
					return bytes;
				}
			} catch (BusinessExceptions e) {
				throw e;
			} catch (Exception e) {
				throw new BusinessExceptions("Não foi possível baixar imagem da URL.", e);
			} finally {
				if (conexao != null) {
					conexao.disconnect();
				}
			}
		}
		throw new BusinessExceptions("Quantidade máxima de redirecionamentos excedida.");
	}

	private URI criarUriImagem(String endereco) {
		try {
			return new URI(endereco);
		} catch (Exception e) {
			throw new BusinessExceptions("URL da imagem inválida.", e);
		}
	}

	private void validarUriImagem(URI uri) {
		String protocolo = uri != null && uri.getScheme() != null ? uri.getScheme() : null;
		String host = uri != null ? normalizarHost(uri.getHost()) : null;
		if (host == null || uri.getUserInfo() != null
				|| !("http".equalsIgnoreCase(protocolo) || "https".equalsIgnoreCase(protocolo))) {
			throw new BusinessExceptions("URL da imagem deve usar http ou https.");
		}

		try {
			InetAddress[] enderecos = InetAddress.getAllByName(host);
			if (enderecos == null || enderecos.length == 0) {
				throw new BusinessExceptions("Host da imagem não localizado.");
			}
			if (!hostNaAllowlist(host)) {
				for (InetAddress endereco : enderecos) {
					if (enderecoPrivado(endereco)) {
						throw new BusinessExceptions("Endereço da imagem não permitido (rede interna).");
					}
				}
			}
		} catch (BusinessExceptions e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessExceptions("Não foi possível resolver o host da imagem.", e);
		}
	}

	private boolean hostNaAllowlist(String host) {
		if (hostsPermitidosImagem == null || hostsPermitidosImagem.trim().isEmpty()) {
			return false;
		}
		for (String permitido : hostsPermitidosImagem.split(",")) {
			if (normalizarHost(permitido).equalsIgnoreCase(host)) {
				return true;
			}
		}
		return false;
	}

	private String normalizarHost(String host) {
		if (host == null) {
			return null;
		}
		String resultado = host.trim().toLowerCase(Locale.ROOT);
		if (resultado.startsWith("[") && resultado.endsWith("]")) {
			resultado = resultado.substring(1, resultado.length() - 1);
		}
		while (resultado.endsWith(".")) {
			resultado = resultado.substring(0, resultado.length() - 1);
		}
		return resultado;
	}

	private boolean enderecoPrivado(InetAddress endereco) {
		if (endereco.isAnyLocalAddress() || endereco.isLoopbackAddress() || endereco.isLinkLocalAddress()
				|| endereco.isSiteLocalAddress() || endereco.isMulticastAddress()) {
			return true;
		}
		byte[] bytes = endereco.getAddress();
		if (bytes.length == 4) {
			return enderecoIpv4Privado(bytes);
		}
		if (bytes.length == 16) {
			if ((bytes[0] & 0xfe) == 0xfc) {
				return true;
			}
			if (ipv4Mapeado(bytes)) {
				byte[] ipv4 = new byte[] { bytes[12], bytes[13], bytes[14], bytes[15] };
				return enderecoIpv4Privado(ipv4);
			}
		}
		return false;
	}

	private boolean ipv4Mapeado(byte[] bytes) {
		for (int i = 0; i < 10; i++) {
			if (bytes[i] != 0) {
				return false;
			}
		}
		return (bytes[10] & 0xff) == 0xff && (bytes[11] & 0xff) == 0xff;
	}

	private boolean enderecoIpv4Privado(byte[] bytes) {
		int primeiro = bytes[0] & 0xff;
		int segundo = bytes[1] & 0xff;
		return primeiro == 0 || primeiro == 10 || primeiro == 127
				|| (primeiro == 169 && segundo == 254)
				|| (primeiro == 172 && segundo >= 16 && segundo <= 31)
				|| (primeiro == 192 && segundo == 168);
	}

	private boolean ehRedirecionamento(int status) {
		return status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308;
	}

	private String validarTipoImagem(String contentType) {
		if (contentType == null || contentType.trim().isEmpty()) {
			throw new BusinessExceptions("Tipo da imagem não informado.");
		}
		String tipo = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		if (!("image/jpeg".equals(tipo) || "image/png".equals(tipo) || "image/webp".equals(tipo)
				|| "image/gif".equals(tipo))) {
			throw new BusinessExceptions("Formato de imagem não suportado. Use JPEG, PNG, WebP ou GIF.");
		}
		return tipo;
	}

	private void validarTamanhoImagem(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			throw new BusinessExceptions("A imagem está vazia.");
		}
		if (bytes.length > TAMANHO_MAXIMO_URL) {
			throw new BusinessExceptions("A imagem excede o tamanho máximo permitido.");
		}
	}

	private void validarBytesImagem(byte[] bytes, String contentType) {
		validarTamanhoImagem(bytes);
		String tipoBytes = tipoPelosBytes(bytes);
		if (tipoBytes == null || !tipoBytes.equals(contentType)) {
			throw new BusinessExceptions("Formato de imagem não suportado. Use JPEG, PNG, WebP ou GIF.");
		}
	}

	private String tipoPelosBytes(byte[] bytes) {
		if (comecaCom(bytes, 0xff, 0xd8, 0xff)) {
			return "image/jpeg";
		}
		if (comecaCom(bytes, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) {
			return "image/png";
		}
		if (comecaCom(bytes, 'G', 'I', 'F', '8', '7', 'a') || comecaCom(bytes, 'G', 'I', 'F', '8', '9', 'a')) {
			return "image/gif";
		}
		if (comecaCom(bytes, 'R', 'I', 'F', 'F') && comecaComAPartirDe(bytes, 8, 'W', 'E', 'B', 'P')) {
			return "image/webp";
		}
		return null;
	}

	private boolean comecaCom(byte[] bytes, int... valores) {
		return comecaComAPartirDe(bytes, 0, valores);
	}

	private boolean comecaComAPartirDe(byte[] bytes, int inicio, int... valores) {
		if (bytes == null || inicio < 0 || bytes.length < inicio + valores.length) {
			return false;
		}
		for (int i = 0; i < valores.length; i++) {
			if ((bytes[inicio + i] & 0xff) != (valores[i] & 0xff)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Exclui o produto e tudo que depende dele (imagens, especificações, cores e tamanhos vinculados),
	 * numa única transação. Sem isso as FKs de tab_item_imagem/especificacao/cor/tamanho barram o DELETE.
	 */
	@Transactional
	public Boolean deletar(Integer idItem) {
		Item item = validarItemExistente(idItem);
		em.createQuery("delete from ItemEspecificacao e where e.item.id = :id").setParameter("id", idItem).executeUpdate();
		em.createQuery("delete from ItemImagem i where i.item.id = :id").setParameter("id", idItem).executeUpdate();
		em.createNativeQuery("delete from apl_estoque.tab_item_cor where id_item = :id").setParameter("id", idItem).executeUpdate();
		em.createNativeQuery("delete from apl_estoque.tab_item_tamanho where id_item = :id").setParameter("id", idItem).executeUpdate();
		em.remove(em.contains(item) ? item : em.merge(item));
		return Boolean.TRUE;
	}

	private Item validarItemExistente(Integer idItem) {
		if (idItem == null) {
			throw new BusinessExceptions("Id do item é obrigatório.");
		}
		Item item = itemRepository.buscar(idItem);
		if (item == null) {
			throw new BusinessExceptions("Item não localizado.");
		}
		return item;
	}

	private void validarEspecificacao(ItemEspecificacaoRequest request) {
		if (request == null) {
			throw new BusinessExceptions("Dados da especificação são obrigatórios.");
		}
		if (Texto.isNaoPreenchido(request.getChave())) {
			throw new BusinessExceptions("Chave da especificação é obrigatória.");
		}
	}

	private ItemImagemDTO toImagemDTO(ItemImagem entidade, Integer idItem, byte[] imagemPrincipal) {
		ItemImagemDTO dto = new ItemImagemDTO();
		dto.setId(entidade.getId());
		dto.setIdItem(idItem);
		dto.setOrdem(entidade.getOrdem());
		dto.setImagem(entidade.getImagem());
		dto.setUrl(entidade.getUrl());
		dto.setPrincipal(entidade.getImagem() != null && imagemPrincipal != null
			&& Arrays.equals(entidade.getImagem(), imagemPrincipal));
		return dto;
	}

	private ItemEspecificacaoDTO toEspecificacaoDTO(ItemEspecificacao entidade, Integer idItem) {
		ItemEspecificacaoDTO dto = new ItemEspecificacaoDTO();
		dto.setId(entidade.getId());
		dto.setIdItem(idItem);
		dto.setChave(entidade.getChave());
		dto.setValor(entidade.getValor());
		return dto;
	}

	private void validar(ItemRequestDTO request) {
		if (request == null) {
			throw new BusinessExceptions("Dados do item são obrigatórios.");
		}

		if (Texto.isNaoPreenchido(request.getNome())) {
			throw new BusinessExceptions("Nome do item é obrigatório.");
		}

		validarQuantidadeNaoNegativa(request.getQuantidadeEstoqueMinimo(), "quantidadeEstoqueMinimo");
		validarQuantidadeNaoNegativa(request.getQuantidadeEstoqueMaximo(), "quantidadeEstoqueMaximo");
		validarQuantidadeNaoNegativa(request.getQuantidadeEstoqueAtual(), "quantidadeEstoqueAtual");
		validarQuantidadeNaoNegativa(request.getMaxParcelas(), "maxParcelas");
		validarQuantidadeNaoNegativa(request.getQtdReviews(), "qtdReviews");
		validarQuantidadeNaoNegativa(request.getQtdVendidos(), "qtdVendidos");

		Integer minimo = request.getQuantidadeEstoqueMinimo();
		Integer maximo = request.getQuantidadeEstoqueMaximo();
		if (minimo != null && maximo != null && minimo > maximo) {
			throw new BusinessExceptions("Quantidade Estoque Mínimo não pode ser maior que quantidade Estoque Máximo.");
		}

		if (request.getValorCusto() != null && request.getValorCusto().signum() < 0) {
			throw new BusinessExceptions("Valor Custo não pode ser negativo.");
		}
		if (request.getValorVenda() != null && request.getValorVenda().signum() < 0) {
			throw new BusinessExceptions("Valor venda não pode ser negativo.");
		}
		if (request.getMargemLucro() != null && request.getMargemLucro().signum() < 0) {
			throw new BusinessExceptions("Margem de lucro não pode ser negativa.");
		}
		if (request.getPrecoAntigo() != null && request.getPrecoAntigo().signum() < 0) {
			throw new BusinessExceptions("Preço antigo não pode ser negativo.");
		}
		if (request.getRating() != null && (request.getRating().signum() < 0
				|| request.getRating().compareTo(java.math.BigDecimal.valueOf(5)) > 0)) {
			throw new BusinessExceptions("Rating deve estar entre 0 e 5.");
		}
	}

	private void validarQuantidadeNaoNegativa(Integer valor, String campo) {
		if (valor != null && valor < 0) {
			throw new BusinessExceptions(campo + " não pode ser negativo.");
		}
	}

	private Item toItem(ItemRequestDTO request, Integer id) {
		Item item = new Item();
		item.setId(id != null ? id : request.getId());
		item.setNome(request.getNome() != null ? request.getNome().trim() : null);
		item.setUnidade(ref(unidadeRepository, request.getIdUnidade(), "idUnidade"));
		item.setIdCategoria(ref(categoriaRepository, request.getIdCategoria(), "idCategoria"));
		item.setIdMarca(ref(marcaRepository, request.getIdMarca(), "idMarca"));
		item.setIdModelo(refModelo(request.getIdModelo(), item));
		item.setCodigoBarras(request.getCodigoBarras());
		item.setDescricao(request.getDescricao());
		item.setData_validade(request.getDataValidade());
		item.setQuantidadeEstoqueMinimo(request.getQuantidadeEstoqueMinimo());
		item.setQuantidadeEstoqueMaximo(request.getQuantidadeEstoqueMaximo());
		item.setQuantidadeEstoqueAtual(request.getQuantidadeEstoqueAtual());
		item.setAtivo(request.isAtivo());
		item.setValorCusto(request.getValorCusto());
		item.setMargemLucro(request.getMargemLucro());
		item.setValorVenda(request.getValorVenda());
		item.setCodigoFornecedor(request.getCodigoFornecedor());
		item.setIcone(request.getIcone());
		item.setPrecoAntigo(request.getPrecoAntigo());
		item.setMaxParcelas(request.getMaxParcelas());
		item.setFull(request.isFull());
		item.setFreteGratis(request.isFreteGratis());
		item.setRating(request.getRating());
		item.setQtdReviews(request.getQtdReviews());
		item.setQtdVendidos(request.getQtdVendidos());
		item.setCores(refs(corRepository, request.getIdsCores(), "idsCores"));
		item.setTamanhos(refs(tamanhoRepository, request.getIdsTamanhos(), "idsTamanhos"));
		return item;
	}

	private <T> List<T> refs(BaseRepository<T> repository, List<Integer> ids, String campo) {
		List<T> lista = new ArrayList<>();
		if (ids == null || ids.isEmpty()) {
			return lista;
		}
		for (Integer id : ids) {
			if (id == null) {
				continue;
			}
			lista.add(ref(repository, id, campo));
		}
		return lista;
	}

	private <T> T ref(BaseRepository<T> repository, Integer id, String campo) {
		if (id == null) {
			return null;
		}
		return repository.findById(id)
				.orElseThrow(() -> new BusinessExceptions(campo + " inv\u00e1lido: registro n\u00e3o encontrado."));
	}

	/** Busca o modelo e garante que pertence a marca informada (quando ambos tem marca). */
	private Modelo refModelo(Integer idModelo, Item item) {
		Modelo modelo = ref(modeloRepository, idModelo, "idModelo");
		if (modelo != null && modelo.getMarca() != null && item.getIdMarca() != null
				&& !modelo.getMarca().getId().equals(item.getIdMarca().getId())) {
			throw new BusinessExceptions("idModelo inv\u00e1lido: o modelo n\u00e3o pertence \u00e0 marca informada.");
		}
		return modelo;
	}
}
