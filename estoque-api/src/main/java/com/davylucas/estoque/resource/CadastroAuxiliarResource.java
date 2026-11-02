package com.davylucas.estoque.resource;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.davylucas.estoque.domain.CadastroAuxiliar;
import com.davylucas.estoque.exceptions.BusinessExceptions;
import com.davylucas.estoque.repository.CadastroAuxiliarRepository;
import com.davylucas.estoque.util.Texto;

/**
 * CRUD simples dos cadastros auxiliares. Erros de negocio viram BusinessExceptions (HTTP 409).
 * GET listar[?ativos=true] | GET buscar/{id} | POST inserir | PUT {id}/atualizar | DELETE deletar/{id}
 */
public abstract class CadastroAuxiliarResource<T extends CadastroAuxiliar> {

	protected final CadastroAuxiliarRepository<T> repository;
	protected final String nome;

	@PersistenceContext
	private EntityManager em;

	protected CadastroAuxiliarResource(CadastroAuxiliarRepository<T> repository, String nome) {
		this.repository = repository;
		this.nome = nome;
	}

	@GetMapping("listar")
	public List<T> listar(@RequestParam(value = "ativos", required = false) Boolean ativos) {
		if (Boolean.TRUE.equals(ativos)) {
			return repository.findByAtivoTrueOrderByDescricaoAsc();
		}
		return repository.findAllByOrderByDescricaoAsc();
	}

	@GetMapping("buscar/{id}")
	public T buscar(@PathVariable("id") Integer id) {
		if (id == null) {
			throw new BusinessExceptions("Id \u00e9 obrigat\u00f3rio.");
		}
		return repository.findById(id)
				.orElseThrow(() -> new BusinessExceptions(nome + " n\u00e3o localizado(a)."));
	}

	@PostMapping("inserir")
	public T inserir(@RequestBody T entidade) {
		validar(entidade);
		entidade.setId(null);
		prepararParaSalvar(entidade);
		return repository.save(entidade);
	}

	@PutMapping("{id}/atualizar")
	public T atualizar(@PathVariable("id") Integer id, @RequestBody T entidade) {
		buscar(id);
		validar(entidade);
		entidade.setId(id);
		prepararParaSalvar(entidade);
		return repository.save(entidade);
	}

	@DeleteMapping("deletar/{id}")
	public Boolean deletar(@PathVariable("id") Integer id) {
		buscar(id);
		String emUso = mensagemEmUso(id);
		if (emUso != null) {
			throw new BusinessExceptions(emUso);
		}
		try {
			repository.deleteById(id);
		} catch (DataAccessException | TransactionSystemException e) {
			throw new BusinessExceptions(nome + " est\u00e1 em uso e n\u00e3o pode ser removido(a).", e);
		}
		return Boolean.TRUE;
	}

	/** Regra de negócio: não exclui cadastro referenciado por produtos (ou marca com modelos). */
	private String mensagemEmUso(Integer id) {
		long itens;
		long modelos = 0;
		switch (nome) {
		case "Marca":
			itens = contar("select count(i) from Item i where i.idMarca.id = :id", id);
			modelos = contar("select count(m) from Modelo m where m.marca.id = :id", id);
			break;
		case "Categoria":
			itens = contar("select count(i) from Item i where i.idCategoria.id = :id", id);
			break;
		case "Unidade":
			itens = contar("select count(i) from Item i where i.unidade.id = :id", id);
			break;
		case "Modelo":
			itens = contar("select count(i) from Item i where i.idModelo.id = :id", id);
			break;
		case "Cor":
			itens = contarNativo("select count(*) from apl_estoque.tab_item_cor where id_cor = :id", id);
			break;
		case "Tamanho":
			itens = contarNativo("select count(*) from apl_estoque.tab_item_tamanho where id_tamanho = :id", id);
			break;
		default:
			return null;
		}
		if (itens == 0 && modelos == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder(nome).append(" em uso por ");
		if (itens > 0) {
			sb.append(itens).append(" produto(s)");
		}
		if (modelos > 0) {
			sb.append(itens > 0 ? " e " : "").append(modelos).append(" modelo(s)");
		}
		return sb.append(". Remova ou altere esses registros antes de excluir.").toString();
	}

	private long contar(String jpql, Integer id) {
		return ((Number) em.createQuery(jpql).setParameter("id", id).getSingleResult()).longValue();
	}

	private long contarNativo(String sql, Integer id) {
		return ((Number) em.createNativeQuery(sql).setParameter("id", id).getSingleResult()).longValue();
	}

	protected void validar(T entidade) {
		if (entidade == null) {
			throw new BusinessExceptions("Dados de " + nome + " s\u00e3o obrigat\u00f3rios.");
		}
		if (Texto.isNaoPreenchido(entidade.getDescricao())) {
			throw new BusinessExceptions("Descri\u00e7\u00e3o de " + nome + " \u00e9 obrigat\u00f3ria.");
		}
		entidade.setDescricao(entidade.getDescricao().trim());
		if (entidade.getDescricao().length() > 255) {
			throw new BusinessExceptions("Descri\u00e7\u00e3o de " + nome + " excede 255 caracteres.");
		}
	}

	/** Gancho para resolver associacoes antes do save. */
	protected void prepararParaSalvar(T entidade) {
	}
}