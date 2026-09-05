package com.example.AmazonS3RDSService.exception;

import java.util.List;
import java.util.UUID;

public class ProcessingException extends RuntimeException {
    private final String code;
    private final List<ValidationDetail> details;
    private UUID requestId;

    public ProcessingException(String code, String message) {
        this(code, message, List.of(), null);
    }

    public ProcessingException(String code, String message, List<ValidationDetail> details) {
        this(code, message, details, null);
    }

    public ProcessingException(String code, String message, Throwable cause) {
        this(code, message, List.of(), cause);
    }

    private ProcessingException(String code, String message, List<ValidationDetail> details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = List.copyOf(details);
    }

    public String getCode() { return code; }
    public List<ValidationDetail> getDetails() { return details; }
    public UUID getRequestId() { return requestId; }
    public ProcessingException withRequestId(UUID value) { requestId = value; return this; }
}
