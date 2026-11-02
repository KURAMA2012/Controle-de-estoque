package com.davylucas.estoque.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "tab_item", schema = "apl_estoque")
public class Item {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "nome")
	public String nome;

	@JoinColumn(name = "id_unidade")
	@ManyToOne(cascade = CascadeType.REFRESH)
	public Unidade unidade;

	@JoinColumn(name = "id_categoria")
	@ManyToOne(cascade = CascadeType.REFRESH)
	public Categoria idCategoria;

	@JoinColumn(name = "id_marca")
	@ManyToOne(cascade = CascadeType.REFRESH)
	public Marca idMarca;

	@JoinColumn(name = "id_modelo")
	@ManyToOne(cascade = CascadeType.REFRESH)
	public Modelo idModelo;

	@Column(name = "codigo_barras")
	public String codigoBarras;

	/** Bytes da imagem principal: nao trafega no JSON (GET item/{id}/imagem-principal). */
	@Column(name = "imagem")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public byte[] imagem;

	@Column(name = "nm_icone")
	public String icone;

	@Column(name = "descricao")
	public String descricao;

	@Column(name = "data_validade")
	public LocalDate data_validade;

	@Column(name = "quantidade_estoque_minimo")
	public Integer quantidadeEstoqueMinimo;

	@Column(name = "quantidade_estoque_maximo")
	public Integer quantidadeEstoqueMaximo;

	@Column(name = "quantidade_estoque_atual")
	public Integer quantidadeEstoqueAtual;

	@Column(name = "fl_ativo")
	public boolean ativo;

	@Column(name = "vlr_custo")
	public BigDecimal valorCusto;

	@Column(name = "vlr_margem_lucro")
	public BigDecimal margemLucro;

	@Column(name = "vlr_venda")
	public BigDecimal valorVenda;

	@Column(name = "vlr_preco_antigo")
	public BigDecimal precoAntigo;

	@Column(name = "nr_max_parcelas")
	public Integer maxParcelas;

	@Column(name = "fl_full")
	public boolean full;

	@Column(name = "fl_frete_gratis")
	public boolean freteGratis;

	@Column(name = "vlr_rating")
	public BigDecimal rating;

	@Column(name = "qtd_reviews")
	public Integer qtdReviews;

	@Column(name = "qtd_vendidos")
	public Integer qtdVendidos;

	@Column(name = "cd_fornecedor")
	public String codigoFornecedor;

	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable(name = "tab_item_cor", schema = "apl_estoque", joinColumns = {
			@JoinColumn(name = "id_item") }, inverseJoinColumns = { @JoinColumn(name = "id_cor") })
	public List<Cor> cores;

	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable(name = "tab_item_tamanho", schema = "apl_estoque", joinColumns = {
			@JoinColumn(name = "id_item") }, inverseJoinColumns = { @JoinColumn(name = "id_tamanho") })
	public List<Tamanho> tamanhos;

	/**
	 * Galeria adicional (alem de imagem/icone). Sem orphanRemoval: atualizacao
	 * generica do Item nao deve reatribuir esta colecao — gerenciada a parte.
	 */
	@JsonIgnore
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "item")
	public List<ItemImagem> imagens;

	/** Especificacoes tecnicas (chave/valor), mesma logica de imagens. */
	@JsonIgnore
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "item")
	public List<ItemEspecificacao> especificacoes;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public Categoria getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(Categoria idCategoria) {
		this.idCategoria = idCategoria;
	}

	public Marca getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(Marca idMarca) {
		this.idMarca = idMarca;
	}

	public Modelo getIdModelo() {
		return idModelo;
	}

	public void setIdModelo(Modelo idModelo) {
		this.idModelo = idModelo;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}

	/** Indica se ha imagem principal (o front busca os bytes em item/{id}/imagem-principal). */
	@JsonProperty(value = "possuiImagem", access = JsonProperty.Access.READ_ONLY)
	public boolean isPossuiImagem() {
		return imagem != null && imagem.length > 0;
	}

	/** Versao da imagem principal (hash dos bytes), usada para invalidar cache no front. */
	@JsonProperty(value = "versaoImagem", access = JsonProperty.Access.READ_ONLY)
	public String getVersaoImagem() {
		return isPossuiImagem() ? Integer.toHexString(java.util.Arrays.hashCode(imagem)) : null;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public LocalDate getData_validade() {
		return data_validade;
	}

	public void setData_validade(LocalDate localDate) {
		this.data_validade = localDate;
	}

	public Integer getQuantidadeEstoqueMinimo() {
		return quantidadeEstoqueMinimo;
	}

	public void setQuantidadeEstoqueMinimo(Integer quantidadeEstoqueMinimo) {
		this.quantidadeEstoqueMinimo = quantidadeEstoqueMinimo;
	}

	public Integer getQuantidadeEstoqueMaximo() {
		return quantidadeEstoqueMaximo;
	}

	public void setQuantidadeEstoqueMaximo(Integer quantidadeEstoqueMaximo) {
		this.quantidadeEstoqueMaximo = quantidadeEstoqueMaximo;
	}

	public Integer getQuantidadeEstoqueAtual() {
		return quantidadeEstoqueAtual;
	}

	public void setQuantidadeEstoqueAtual(Integer quantidadeEstoqueAtual) {
		this.quantidadeEstoqueAtual = quantidadeEstoqueAtual;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public BigDecimal getValorCusto() {
		return valorCusto;
	}

	public void setValorCusto(BigDecimal valorCusto) {
		this.valorCusto = valorCusto;
	}

	public BigDecimal getMargemLucro() {
		return margemLucro;
	}

	public void setMargemLucro(BigDecimal margemLucro) {
		this.margemLucro = margemLucro;
	}

	public BigDecimal getValorVenda() {
		return valorVenda;
	}

	public void setValorVenda(BigDecimal valorVenda) {
		this.valorVenda = valorVenda;
	}

	public BigDecimal getPrecoAntigo() {
		return precoAntigo;
	}

	public void setPrecoAntigo(BigDecimal precoAntigo) {
		this.precoAntigo = precoAntigo;
	}

	public Integer getMaxParcelas() {
		return maxParcelas;
	}

	public void setMaxParcelas(Integer maxParcelas) {
		this.maxParcelas = maxParcelas;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public boolean isFreteGratis() {
		return freteGratis;
	}

	public void setFreteGratis(boolean freteGratis) {
		this.freteGratis = freteGratis;
	}

	public BigDecimal getRating() {
		return rating;
	}

	public void setRating(BigDecimal rating) {
		this.rating = rating;
	}

	public Integer getQtdReviews() {
		return qtdReviews;
	}

	public void setQtdReviews(Integer qtdReviews) {
		this.qtdReviews = qtdReviews;
	}

	public Integer getQtdVendidos() {
		return qtdVendidos;
	}

	public void setQtdVendidos(Integer qtdVendidos) {
		this.qtdVendidos = qtdVendidos;
	}

	public String getCodigoFornecedor() {
		return codigoFornecedor;
	}

	public void setCodigoFornecedor(String codigoFornecedor) {
		this.codigoFornecedor = codigoFornecedor;
	}

	public List<Cor> getCores() {
		return cores;
	}

	public void setCores(List<Cor> cores) {
		this.cores = cores;
	}

	public List<Tamanho> getTamanhos() {
		return tamanhos;
	}

	public void setTamanhos(List<Tamanho> tamanhos) {
		this.tamanhos = tamanhos;
	}

	@JsonIgnore
	public List<ItemImagem> getImagens() {
		return imagens;
	}

	public void setImagens(List<ItemImagem> imagens) {
		this.imagens = imagens;
	}

	@JsonIgnore
	public List<ItemEspecificacao> getEspecificacoes() {
		return especificacoes;
	}

	public void setEspecificacoes(List<ItemEspecificacao> especificacoes) {
		this.especificacoes = especificacoes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		return Objects.equals(id, other.id);
	}

}
