package com.example.AmazonS3RDSService.dto;

import com.example.AmazonS3RDSService.validation.ValidXmlFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class FileUploadRequest {

    @ValidXmlFile
    private MultipartFile file;

    @NotBlank(message = "Company ID is mandatory")
    @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{9}[0-9]$", message = "Company ID must be a valid ISIN")
    @Size(max = 100, message = "Company ID must not exceed 100 characters")
    private String companyId;

    @NotBlank(message = "Company name is mandatory")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @NotNull(message = "Date is mandatory")
    @PastOrPresent(message = "Date must not be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
}
