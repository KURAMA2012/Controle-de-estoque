package com.davylucas.estoque.util;

public final class Texto {

	private Texto() {
	}

	public static boolean isNaoPreenchido(String value) {
		return value == null || value.trim().length() == 0;
	}
}