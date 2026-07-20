package com.example.S3_RDS_CloudWatch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class EmailNotificationService {

    private final SesClient sesClient;
    private final String senderEmail;

    public EmailNotificationService(SesClient sesClient,
                                    @Value("${cloud.ses.sender-email:}") String senderEmail) {
        this.sesClient = sesClient;
        this.senderEmail = senderEmail;
    }

    public void sendUploadConfirmation(String recipientName, String recipientEmail, String fileName) {
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("SES sender email is not configured");
        }
        if (!senderEmail.contains("@")) {
            throw new IllegalStateException("cloud.ses.sender-email must be an email address, not an SES identity ARN");
        }

        String subject = "Your file upload was successful";
        String body = "Hello " + recipientName + ",\n\n"
                + "Your file '" + fileName + "' has been uploaded successfully.\n\n"
                + "Regards,\nFile Service";

        sesClient.sendEmail(SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(recipientEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder().text(Content.builder().data(body).charset("UTF-8").build()).build())
                        .build())
                .build());
    }
}
