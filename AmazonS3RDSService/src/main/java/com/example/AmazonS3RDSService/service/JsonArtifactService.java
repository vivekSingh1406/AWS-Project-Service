package com.example.AmazonS3RDSService.service;

import com.example.AmazonS3RDSService.exception.ProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonArtifactService {
    private final ObjectMapper mapper;
    public JsonArtifactService(ObjectMapper mapper) { this.mapper = mapper; }
    public byte[] serialize(Object value) {
        try { return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value); }
        catch (JsonProcessingException exception) {
            throw new ProcessingException("JSON_GENERATION_FAILED", "Validated record could not be serialized", exception);
        }
    }
}
