package com.example.AmazonS3RDSService.exception;

public record ValidationDetail(String field, String message, Integer line, Integer column) {
    public static ValidationDetail field(String field, String message) {
        return new ValidationDetail(field, message, null, null);
    }
}
