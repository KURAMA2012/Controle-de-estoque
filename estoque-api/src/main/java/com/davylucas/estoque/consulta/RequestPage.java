package com.davylucas.estoque.consulta;

import java.util.List;

/** Corpo do POST /item/consultar (paginacao, ordenacao e filtros). */
public class RequestPage implements java.io.Serializable {

	private static final long serialVersionUID = 8188292048028418096L;

	private Object filtros;
	private Integer page;
	private Integer size;
	private List<Filtro> parametros;
	private String orderBy;
	/** "asc" (padrao) ou "desc". Tambem aceito no orderBy: "campo,desc", "campo desc" ou "-campo". */
	private String direcao;
	private Integer offSet;
	private boolean exportaPDF;
	private boolean exportExcel;

	public Object getFiltros() {
		return filtros;
	}

	public void setFiltros(Object filtros) {
		this.filtros = filtros;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public List<Filtro> getParametros() {
		return parametros;
	}

	public void setParametros(List<Filtro> parametros) {
		this.parametros = parametros;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getDirecao() {
		return direcao;
	}

	public void setDirecao(String direcao) {
		this.direcao = direcao;
	}

	public Integer getOffSet() {
		return offSet;
	}

	public void setOffSet(Integer offSet) {
		this.offSet = offSet;
	}

	public boolean isExportaPDF() {
		return exportaPDF;
	}

	public void setExportaPDF(boolean exportaPDF) {
		this.exportaPDF = exportaPDF;
	}

	public boolean isExportExcel() {
		return exportExcel;
	}

	public void setExportExcel(boolean exportExcel) {
		this.exportExcel = exportExcel;
	}
}