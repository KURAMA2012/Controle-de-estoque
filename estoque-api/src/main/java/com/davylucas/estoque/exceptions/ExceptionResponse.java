package com.davylucas.estoque.exceptions;

/** Corpo de erro: {"codeError","requestedURI","dataHora","message"}. */
public class ExceptionResponse {

	private Integer codeError;
	private String requestedURI;
	private String dataHora;
	private String message;

	public Integer getCodeError() {
		return codeError;
	}

	public void setCodeError(Integer codeError) {
		this.codeError = codeError;
	}

	public String getRequestedURI() {
		return requestedURI;
	}

	public void setRequestedURI(String requestedURI) {
		this.requestedURI = requestedURI;
	}

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}