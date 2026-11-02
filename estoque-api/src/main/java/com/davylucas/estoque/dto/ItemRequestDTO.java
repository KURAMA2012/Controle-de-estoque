package com.davylucas.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ItemRequestDTO {

    private Integer id;

    private String nome;

    private Integer idUnidade;

    private Integer idCategoria;

    private Integer idMarca;

    private Integer idModelo;

    private String codigoBarras;

    private byte[] imagem;

    private String icone;

    private String descricao;

    private LocalDate dataValidade;

    private Integer quantidadeEstoqueMinimo;

    private Integer quantidadeEstoqueMaximo;

    private Integer quantidadeEstoqueAtual;

    private boolean ativo;

    private BigDecimal valorCusto;

    private BigDecimal margemLucro;

    private BigDecimal valorVenda;

    private BigDecimal precoAntigo;

    private Integer maxParcelas;

    private boolean full;

    private boolean freteGratis;

    private BigDecimal rating;

    private Integer qtdReviews;

    private Integer qtdVendidos;

    private String codigoFornecedor;

    /** Ids de Cor (tab_cor). Colecao gerenciada no Item. */
    private List<Integer> idsCores;

    /** Ids de Tamanho (tab_tamanho). */
    private List<Integer> idsTamanhos;

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

    public Integer getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Integer idUnidade) {
        this.idUnidade = idUnidade;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public Integer getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(Integer idModelo) {
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

    public LocalDate getDataValidade() {
		return dataValidade;
	}

	public void setDataValidade(LocalDate dataValidade) {
		this.dataValidade = dataValidade;
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

    public List<Integer> getIdsCores() {
        return idsCores;
    }

    public void setIdsCores(List<Integer> idsCores) {
        this.idsCores = idsCores;
    }

    public List<Integer> getIdsTamanhos() {
        return idsTamanhos;
    }

    public void setIdsTamanhos(List<Integer> idsTamanhos) {
        this.idsTamanhos = idsTamanhos;
    }
}
