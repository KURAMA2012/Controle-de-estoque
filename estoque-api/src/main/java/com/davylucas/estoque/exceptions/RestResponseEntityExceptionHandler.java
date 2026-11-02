package com.davylucas.estoque.exceptions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * FrameworkExceptions (e BusinessExceptions) viram HTTP 409 com corpo ExceptionResponse
 * (codeError=409, requestedURI=context path).
 */
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	@ExceptionHandler(value = { FrameworkExceptions.class })
	protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
		ExceptionResponse body = new ExceptionResponse();
		body.setCodeError(HttpStatus.CONFLICT.value());
		body.setDataHora(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
		body.setMessage(ex.getMessage());
		body.setRequestedURI(request.getContextPath());
		LOG.error("codeError={} requestedURI={} message={}", body.getCodeError(), body.getRequestedURI(),
				body.getMessage(), ex);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8));
		return handleExceptionInternal(ex, body, headers, HttpStatus.CONFLICT, request);
	}
}