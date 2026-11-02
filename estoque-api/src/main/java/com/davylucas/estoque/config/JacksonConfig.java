package com.davylucas.estoque.config;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/** Modulo Jackson com o formato de datas usado pelo front-end. */
@Configuration
public class JacksonConfig {

	private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer bsJacksonCustomizer() {
		return builder -> {
			builder.featuresToEnable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			builder.modulesToInstall(modulo());
		};
	}

	private SimpleModule modulo() {
		SimpleModule module = new SimpleModule("estoque-datas");
		module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
			@Override
			public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider sp) throws IOException {
				gen.writeString(DATA.format(value));
			}
		});
		module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
			@Override
			public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
				return paraLocalDate(p.getValueAsString());
			}
		});
		module.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
				gen.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value));
			}
		});
		module.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
				String v = p.getValueAsString();
				if (v == null || v.trim().isEmpty()) {
					return null;
				}
				return LocalDateTime.parse(v.replaceAll("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			}
		});
		return module;
	}

	static LocalDate paraLocalDate(String valor) {
		if (valor == null || valor.trim().isEmpty()) {
			return null;
		}
		String v = valor.trim();
		if (v.contains("/")) {
			return LocalDate.parse(v, DATA_BR);
		}
		if (v.length() > 10) {
			v = v.substring(0, 10);
		}
		return LocalDate.parse(v, DATA);
	}
}