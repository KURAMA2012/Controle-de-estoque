package com.davylucas.estoque.exceptions;

/** Excecao de regra de negocio. */
public class BusinessExceptions extends FrameworkExceptions {

	private static final long serialVersionUID = 6584072681826049611L;

	public BusinessExceptions() {
		super();
	}

	public BusinessExceptions(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessExceptions(String message) {
		super(message);
	}

	public BusinessExceptions(Throwable cause) {
		super(cause);
	}
}