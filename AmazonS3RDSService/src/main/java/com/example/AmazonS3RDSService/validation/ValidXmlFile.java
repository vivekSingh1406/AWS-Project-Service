package com.example.AmazonS3RDSService.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = XmlFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidXmlFile {
    String message() default "File must be a non-empty XML file no larger than 25 MB";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
