package com.example.AmazonS3RDSService.storage;

import java.util.Map;

public interface ObjectStorage {
    void put(String key, byte[] content, String contentType, Map<String, String> metadata);
}
