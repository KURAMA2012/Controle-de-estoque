package com.davylucas.estoque.consulta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta paginada generica com filtros dinamicos.
 * Filtros: String = like case-insensitive; boolean/Integer/Long = igual; objeto {id}|{codigo} = associacao.
 * totalElements respeita os mesmos filtros da listagem.
 * Ordenacao: orderBy = "campo" (asc), "campo,desc", "campo desc" ou "-campo"; ou orderBy + direcao ("asc"/"desc").
 * Campos de ordenacao inexistentes sao ignorados. Desempate sempre por id (paginacao estavel).
 */
@Component
public class ConsultaPaginada {

	@PersistenceContext
	private EntityManager em;

	@Transactional(readOnly = true)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Page<T> consultar(RequestPage requestPage, Class<T> clazz) {
		List<Filtro> parametros = requestPage.getParametros();
		if (parametros != null && !parametros.isEmpty()) {
			Map filtros = requestPage.getFiltros() instanceof Map ? (Map) requestPage.getFiltros() : new LinkedHashMap();
			for (Filtro parametro : parametros) {
				filtros.put(parametro.getCampo(), parametro.getValor());
			}
			requestPage.setFiltros(filtros);
		}
		int size = requestPage.getSize() != null && requestPage.getSize() > 0 ? requestPage.getSize() : 0;
		int page = requestPage.getPage() != null && requestPage.getPage() > 0 ? requestPage.getPage() : 0;

		CriteriaBuilder builder = em.getCriteriaBuilder();

		CriteriaQuery<T> query = builder.createQuery(clazz);
		Root<T> root = query.from(clazz);
		query.select(root).where(predicado(builder, root, requestPage.getFiltros(), clazz));
		query.orderBy(ordenacao(builder, root, requestPage, clazz));
		TypedQuery<T> typed = em.createQuery(query);
		if (size > 0) {
			typed.setFirstResult(page * size);
			typed.setMaxResults(size);
		}
		List<T> lista = typed.getResultList();

		CriteriaQuery<Long> count = builder.createQuery(Long.class);
		Root<T> rootCount = count.from(clazz);
		count.select(builder.count(rootCount)).where(predicado(builder, rootCount, requestPage.getFiltros(), clazz));
		Long total = em.createQuery(count).getSingleResult();

		if (size <= 0) {
			return new PageImpl<T>(lista);
		}
		return new PageImpl<T>(lista, PageRequest.of(page, size), total);
	}

	private List<Order> ordenacao(CriteriaBuilder builder, Root<?> root, RequestPage requestPage, Class<?> clazz) {
		List<Order> ordens = new ArrayList<>();
		String orderBy = requestPage.getOrderBy() == null ? "" : requestPage.getOrderBy().trim();
		boolean desc = "desc".equalsIgnoreCase(requestPage.getDirecao() == null ? "" : requestPage.getDirecao().trim());
		if (orderBy.startsWith("-")) {
			desc = true;
			orderBy = orderBy.substring(1).trim();
		}
		String[] partes = orderBy.split("[,\\s]+");
		String campo = partes.length > 0 ? partes[0] : "";
		if (partes.length > 1) {
			desc = "desc".equalsIgnoreCase(partes[1]);
		}
		if (!campo.isEmpty() && !"id".equals(campo) && atributoExiste(clazz, campo)) {
			ordens.add(desc ? builder.desc(root.get(campo)) : builder.asc(root.get(campo)));
		}
		if (atributoExiste(clazz, "id")) {
			ordens.add("id".equals(campo) && desc ? builder.desc(root.get("id")) : builder.asc(root.get("id")));
		}
		return ordens;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Predicate predicado(CriteriaBuilder builder, Root root, Object filtrosObj, Class<?> clazz) {
		Predicate predicate = builder.conjunction();
		if (!(filtrosObj instanceof Map)) {
			return predicate;
		}
		Map filtros = (Map) filtrosObj;
		for (Object key : filtros.keySet()) {
			Object valor = filtros.get(key);
			if (valor == null || valor.toString().trim().length() == 0) {
				continue;
			}
			String campo = key.toString();
			if (!atributoExiste(clazz, campo)) {
				continue;
			}
			if (valor instanceof Map) {
				Map nested = (Map) valor;
				if (nested.isEmpty()) {
					continue;
				}
				Object nestedId = nested.get("id");
				String idAttr = "id";
				if (nestedId == null) {
					nestedId = nested.get("codigo");
					idAttr = "codigo";
				}
				if (nestedId == null || String.valueOf(nestedId).trim().isEmpty()
						|| "null".equalsIgnoreCase(String.valueOf(nestedId).trim())) {
					continue;
				}
				Number n = toNumber(nestedId);
				if (n != null) {
					predicate = builder.and(predicate, builder.equal(root.get(campo).get(idAttr), n.intValue()));
				}
				continue;
			}
			Class<?> javaType = root.get(campo).getJavaType();
			if (javaType == String.class) {
				predicate = builder.and(predicate, builder.like(builder.upper(root.get(campo)),
						"%" + valor.toString().toUpperCase() + "%"));
			} else if (javaType == Boolean.class || javaType == boolean.class) {
				predicate = builder.and(predicate, builder.equal(root.get(campo), Boolean.valueOf(valor.toString())));
			} else if (javaType == Integer.class || javaType == int.class) {
				Number n = toNumber(valor);
				if (n != null) {
					predicate = builder.and(predicate, builder.equal(root.get(campo), n.intValue()));
				}
			} else if (javaType == Long.class || javaType == long.class) {
				Number n = toNumber(valor);
				if (n != null) {
					predicate = builder.and(predicate, builder.equal(root.get(campo), n.longValue()));
				}
			} else {
				predicate = builder.and(predicate, builder.equal(root.get(campo), valor));
			}
		}
		return predicate;
	}

	private boolean atributoExiste(Class<?> clazz, String nome) {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.getName().equals(nome)) {
				return true;
			}
		}
		return false;
	}

	private Number toNumber(Object valor) {
		if (valor == null) {
			return null;
		}
		if (valor instanceof Number) {
			return (Number) valor;
		}
		String s = valor.toString().trim();
		if (s.isEmpty()) {
			return null;
		}
		try {
			return s.contains(".") ? Double.valueOf(s) : Long.valueOf(s);
		} catch (Exception e) {
			return null;
		}
	}
}