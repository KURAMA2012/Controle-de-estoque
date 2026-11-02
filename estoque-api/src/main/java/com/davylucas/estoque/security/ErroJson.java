package com.davylucas.estoque.security;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.davylucas.estoque.exceptions.ExceptionResponse;

/** Monta o mesmo corpo ExceptionResponse usado pelo handler global, para 401/403. */
public final class ErroJson {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ErroJson() {
	}

	public static ExceptionResponse corpo(HttpServletRequest request, int status, String mensagem) {
		ExceptionResponse body = new ExceptionResponse();
		body.setCodeError(status);
		body.setRequestedURI(request.getContextPath());
		body.setDataHora(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
		body.setMessage(mensagem);
		return body;
	}

	public static void escrever(HttpServletRequest request, HttpServletResponse response, int status, String mensagem)
			throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		MAPPER.writeValue(response.getOutputStream(), corpo(request, status, mensagem));
	}
}