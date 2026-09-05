package com.example.AmazonS3RDSService.service.Impl;

import com.example.AmazonS3RDSService.dto.FileUploadRequest;
import com.example.AmazonS3RDSService.dto.ProcessResult;
import com.example.AmazonS3RDSService.entity.FileProcessingEntity;
import com.example.AmazonS3RDSService.exception.ProcessingException;
import com.example.AmazonS3RDSService.exception.ValidationDetail;
import com.example.AmazonS3RDSService.service.FileProcessingService;
import com.example.AmazonS3RDSService.service.JsonArtifactService;
import com.example.AmazonS3RDSService.service.ProcessingStateService;
import com.example.AmazonS3RDSService.storage.ObjectStorage;
import com.example.AmazonS3RDSService.validation.ParsingValidation;
import com.example.AmazonS3RDSService.xml.SecureXmlProcessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileProcessingServiceImpl implements FileProcessingService {
    private static final Logger log = LoggerFactory.getLogger(FileProcessingServiceImpl.class);

    private final ParsingValidation fileValidation;
    private final SecureXmlProcessor xmlProcessor;
    private final ObjectStorage storage;
    private final ProcessingStateService state;
    private final JsonArtifactService jsonArtifacts;

    @Override
    public ProcessResult process(FileUploadRequest request) {
        fileValidation.validateAllRequest(request);
        MultipartFile file = request.getFile();
        FileProcessingEntity entity = state.create(request.getCompanyId(), request.getCompanyName(), request.getDate(), file.getOriginalFilename());
        return processArtifacts(read(file), entity);
    }

    private ProcessResult processArtifacts(byte[] xml, FileProcessingEntity received) {
        UUID requestId = received.getRequestId();
        FileProcessingEntity entity = received;
        try {
            Map<String, Object> businessFacts = xmlProcessor.parse(xml);
            validateRequestMatchesXml(received, businessFacts);
            byte[] json = jsonArtifacts.serialize(businessFacts);
            String xmlFileName = entity.getXmlFileName();
            String jsonFileName = toJsonFileName(xmlFileName);
            String xmlKey = "xml/files/" + requestId + "/" + xmlFileName;
            String jsonKey = "json/files/" + requestId + "/" + jsonFileName;
            Map<String, String> metadata = Map.of(
                    "request-id", requestId.toString(),
                    "company-id", entity.getCompanyId());

            storage.put(xmlKey, xml, "application/xml", metadata);
            entity = state.markXmlStored(requestId, xmlKey);

            storage.put(jsonKey, json, "application/json", metadata);
            entity = state.markJsonStored(requestId, jsonFileName, jsonKey);

            ProcessResult result = ProcessResult.from(state.markCompleted(requestId), false);
            log.info("file_processing_completed requestId={} status={}", requestId, result.status());
            return result;
        } catch (ProcessingException exception) {
            markFailed(requestId, exception);
            throw exception.withRequestId(requestId);
        } catch (RuntimeException exception) {
            ProcessingException safe = new ProcessingException(
                    "PROCESSING_FAILED", "Request processing failed", exception).withRequestId(requestId);
            markFailed(requestId, safe);
            throw safe;
        }
    }

    private void validateRequestMatchesXml(FileProcessingEntity request, Map<String, Object> facts) {
        List<ValidationDetail> errors = new ArrayList<>();
        String xmlCompanyName = fact(facts, "NameOfTheCompany");
        String xmlCompanyId = fact(facts, "ISIN");
        String xmlDate = fact(facts, "DateOfReport");

        if (!normalize(request.getCompanyName()).equalsIgnoreCase(normalize(xmlCompanyName))) {
            errors.add(ValidationDetail.field("companyName", "Company name does not match NameOfTheCompany in XML"));
        }
        if (!request.getCompanyId().trim().equalsIgnoreCase(xmlCompanyId.trim())) {
            errors.add(ValidationDetail.field("companyId", "Company ID does not match ISIN in XML"));
        }
        try {
            if (!request.getSubmittedDate().equals(LocalDate.parse(xmlDate))) {
                errors.add(ValidationDetail.field("date", "Date does not match DateOfReport in XML"));
            }
        } catch (DateTimeParseException exception) {
            errors.add(ValidationDetail.field("DateOfReport", "DateOfReport must use yyyy-MM-dd format"));
        }

        if (!errors.isEmpty()) {
            throw new ProcessingException("REQUEST_XML_MISMATCH",
                    "Multipart fields do not match the uploaded XML", errors);
        }
    }

    private String fact(Map<String, Object> facts, String name) {
        return String.valueOf(facts.get(name));
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private String toJsonFileName(String xmlFileName) {
        int extension = xmlFileName.lastIndexOf('.');
        String baseName = extension > 0 ? xmlFileName.substring(0, extension) : xmlFileName;
        return baseName + ".json";
    }

    private void markFailed(UUID requestId, ProcessingException failure) {
        try {
            state.markFailed(requestId, failure.getCode(), failure.getMessage());
        } catch (RuntimeException stateFailure) {
            failure.addSuppressed(stateFailure);
        }
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ProcessingException("INVALID_FILE", "XML file could not be read", exception);
        }
    }
}
