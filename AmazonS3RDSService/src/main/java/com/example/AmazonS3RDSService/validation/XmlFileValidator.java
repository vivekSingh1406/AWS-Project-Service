package com.example.AmazonS3RDSService.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

public class XmlFileValidator implements ConstraintValidator<ValidXmlFile, MultipartFile> {
    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;
    private static final Set<String> XML_CONTENT_TYPES = Set.of("application/xml", "text/xml");

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xml")) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        String normalizedContentType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return XML_CONTENT_TYPES.contains(normalizedContentType);
    }
}
