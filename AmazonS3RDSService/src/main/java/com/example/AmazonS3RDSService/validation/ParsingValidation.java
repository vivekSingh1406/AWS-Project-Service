package com.example.AmazonS3RDSService.validation;

import com.example.AmazonS3RDSService.dto.FileUploadRequest;
import com.example.AmazonS3RDSService.exception.ProcessingException;
import com.example.AmazonS3RDSService.exception.ValidationDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ParsingValidation {
    private static final Pattern SAFE_XML_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*\\.[xX][mM][lL]");
    private static final Pattern ISIN = Pattern.compile("[A-Z]{2}[A-Z0-9]{9}[0-9]");
    private final FileValidationProperties properties;

    public ParsingValidation(FileValidationProperties properties) {
        this.properties = properties;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProcessingException("MISSING_FILE", "XML file is required");
        }
        if (file.getSize() > properties.maxSize().toBytes()) {
            throw invalid("XML file exceeds the configured size limit");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !SAFE_XML_NAME.matcher(filename).matches()) {
            throw invalid("XML filename is invalid");
        }
        String contentType = file.getContentType();
        String normalized = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        boolean supported = properties.allowedContentTypes().stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).anyMatch(normalized::equals);
        if (!supported) {
            throw invalid("XML content type is not supported");
        }
    }

    public void validateAllRequest(FileUploadRequest request) {
        List<ValidationDetail> errors = new ArrayList<>();
        if (request == null) {
            errors.add(ValidationDetail.field("request", "Request is required"));
            throw invalidRequest(errors);
        }

        validateFile(request.getFile(), errors);

        String companyId = request.getCompanyId();
        if (companyId == null || companyId.isBlank()) {
            errors.add(ValidationDetail.field("companyId", "Company ID is required"));
        } else if (!ISIN.matcher(companyId.trim().toUpperCase(Locale.ROOT)).matches()) {
            errors.add(ValidationDetail.field("companyId", "Company ID must be a valid ISIN"));
        }

        String companyName = request.getCompanyName();
        if (companyName == null || companyName.isBlank()) {
            errors.add(ValidationDetail.field("companyName", "Company name is required"));
        } else if (companyName.trim().length() > 200) {
            errors.add(ValidationDetail.field("companyName", "Company name must not exceed 200 characters"));
        }

        LocalDate date = request.getDate();
        if (date == null) {
            errors.add(ValidationDetail.field("date", "Date is required"));
        } else if (date.isAfter(LocalDate.now())) {
            errors.add(ValidationDetail.field("date", "Date must not be in the future"));
        }

        if (!errors.isEmpty()) {
            throw invalidRequest(errors);
        }
    }

    private void validateFile(MultipartFile file, List<ValidationDetail> errors) {
        if (file == null || file.isEmpty()) {
            errors.add(ValidationDetail.field("file", "XML file is required and must not be empty"));
            return;
        }
        if (file.getSize() > properties.maxSize().toBytes()) {
            errors.add(ValidationDetail.field("file", "XML file exceeds the configured size limit"));
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !SAFE_XML_NAME.matcher(filename).matches()) {
            errors.add(ValidationDetail.field("file", "XML filename is invalid"));
        }
        String contentType = file.getContentType();
        String normalized = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        boolean supported = properties.allowedContentTypes().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalized::equals);
        if (!supported) {
            errors.add(ValidationDetail.field("file", "XML content type is not supported"));
        }
    }

    private ProcessingException invalidRequest(List<ValidationDetail> errors) {
        return new ProcessingException("INVALID_REQUEST", "Request validation failed", errors);
    }

    private ProcessingException invalid(String message) {
        return new ProcessingException("INVALID_FILE", message);
    }
}
