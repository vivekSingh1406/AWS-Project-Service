package com.example.AmazonS3RDSService.exception;

import com.example.AmazonS3RDSService.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.BindException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProcessingException.class)
    ResponseEntity<ApiErrorResponse> processing(ProcessingException failure, HttpServletRequest request) {
        HttpStatus status = status(failure.getCode());
        log.warn("processing_failed code={} requestId={}", failure.getCode(), failure.getRequestId());
        return response(status, failure.getCode(), failure.getMessage(), failure.getRequestId(), failure.getDetails(), request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiErrorResponse> missingHeader(MissingRequestHeaderException failure, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_HEADER", "Required request header is missing", null, List.of(), request);
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ApiErrorResponse> validation(BindException failure, HttpServletRequest request) {
        List<ValidationDetail> details = failure.getBindingResult().getFieldErrors().stream()
                .map(error -> ValidationDetail.field(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Required request data is missing or invalid", null, details, request);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiErrorResponse> badRequest(Exception failure, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Required request data is missing or invalid", null, List.of(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> tooLarge(MaxUploadSizeExceededException failure, HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Uploaded file exceeds the size limit", null, List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception failure, HttpServletRequest request) {
        log.error("unexpected_processing_failure type={}", failure.getClass().getSimpleName());
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", null, List.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message,
                                                       java.util.UUID requestId, List<com.example.AmazonS3RDSService.exception.ValidationDetail> details,
                                                       HttpServletRequest request) {
        Object correlation = request.getAttribute("correlationId");
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(),
                correlation == null ? null : correlation.toString(), requestId, status.value(), code, message, details));
    }

    private HttpStatus status(String code) {
        if ("NOT_FOUND".equals(code)) return HttpStatus.NOT_FOUND;
        if ("IDEMPOTENCY_CONFLICT".equals(code) || "PROCESSING_IN_PROGRESS".equals(code)) return HttpStatus.CONFLICT;
        if ("DOWNSTREAM_TIMEOUT".equals(code)) return HttpStatus.GATEWAY_TIMEOUT;
        if (code.startsWith("DOWNSTREAM_") || code.startsWith("S3_")) return HttpStatus.BAD_GATEWAY;
        if (code.equals("PROCESSING_FAILED") || code.endsWith("GENERATION_FAILED")) return HttpStatus.INTERNAL_SERVER_ERROR;
        return HttpStatus.BAD_REQUEST;
    }
}
