package com.davylucas.estoque.exceptions;

/** Excecao base tratada pelo RestResponseEntityExceptionHandler (HTTP 409). */
public class FrameworkExceptions extends RuntimeException {

	private static final long serialVersionUID = 8528078029067494799L;

	public FrameworkExceptions() {
		super();
	}

	public FrameworkExceptions(String message, Throwable cause) {
		super(message, cause);
	}

	public FrameworkExceptions(String message) {
		super(message);
	}

	public FrameworkExceptions(Throwable cause) {
		super(cause);
	}
}