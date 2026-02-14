package com.satish.resumebuilderapi.controller;

import com.satish.resumebuilderapi.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
@Slf4j
public class EmailController {

    private final EmailService emailService;
    
    @PostMapping(value = "/send-resume" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendResumeByEmail(
            @RequestPart("recipientEmail") String recipientemail,
            @RequestPart("subject")String subject,
            @RequestPart("message") String message,
            @RequestPart("pdfFile") MultipartFile pdfFile) throws IOException, MessagingException {

//        step 1: validate the inputs
        Map<String, Object> response = new HashMap<>();
        if (Objects.isNull(recipientemail)  || Objects.isNull(pdfFile)){
            response.put("success", false);
            response.put("message", "Missing required fields");
            return ResponseEntity.badRequest().body(response);
        }

//        step 2: get the file data
        byte[] pdfBytes = pdfFile.getBytes();
        String originalFilename = pdfFile.getOriginalFilename();
        String filename = Objects.nonNull(originalFilename) ? originalFilename : "resume.pdf";

//        step 3: prepare the email content
        String emailSubject = Objects.nonNull(subject) ? subject : "Resume Application";
        String emailBody = Objects.nonNull(message) ? message : "Please find my resume attached.\n\n Best regards.";

//        step 4: cal the service method
        emailService.sendEmailWithAttachment(recipientemail, emailSubject, emailBody, pdfBytes, filename);

//        step 5: return the response
        response.put("success", true);
        response.put("message", "Resume sent successfully to " + recipientemail);
        return ResponseEntity.ok(response);

    }
}
