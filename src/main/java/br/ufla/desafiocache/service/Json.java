package br.ufla.desafiocache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Converte objetos para texto (e de volta) para guardar no Redis.
 * NÃO PRECISA MEXER NESTE ARQUIVO.
 */
@Component
public class Json {

    private final ObjectMapper mapper;

    public Json(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String escrever(Object valor) {
        try {
            return mapper.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T ler(String texto, Class<T> tipo) {
        try {
            return mapper.readValue(texto, tipo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T ler(String texto, TypeReference<T> tipo) {
        try {
            return mapper.readValue(texto, tipo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
