package com.nghhieu27.mail.demo.controller;

import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.request.SearchRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.service.EmailService;
import jakarta.mail.Quota;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mail")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping(value = "/sendmail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> sendmail(
            @RequestParam String to,
            @RequestParam String sub,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) MultipartFile attachment
    ) {
        try {
            log.info("Received request to send email to: {}", to);
            if (attachment != null) {
                log.info("Attachment received: {}", attachment.getOriginalFilename());
            }

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(to)
                    .sub(sub)
                    .body(body)
                    .build();

            emailService.sendMail(emailRequest, attachment);
            log.info("Email sent successfully to: {}", to);

            return ApiResponse.builder()
                    .code(1000)
                    .message("Successfully!")
                    .build();

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message("Error sending mail!: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/attachment")
    ResponseEntity<Resource> downloadAttachment (@RequestParam String path) {
        log.info("Downloading sent mail attachment from path: {}", path);
        return emailService.downloadAttachment(path);
    }

    @GetMapping("/email/inbox/{uid}/attachment")
    public ResponseEntity<?> downloadInboxAttachment(
            @PathVariable String uid,
            @RequestParam(required = false) String filename
    ) {
        log.info("Downloading inbox attachment for UID: {}, filename: {}", uid, filename);
        return emailService.streamInboxAttachment(uid, filename);
    }

    @PostMapping("/createmail")
    ApiResponse<EmailResponse> createMail(@Valid @RequestBody EmailRequest emailRequest){
        log.info("Creating mail entry for user: {}", emailRequest.getTo());
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .message("Create successfully!")
                .result(emailService.createMail(emailRequest))
                .build();
    }

    @GetMapping("/inbox")
    ApiResponse<List<EmailResponse>> getInboxs(){
        log.info("Fetching inbox emails");
        return ApiResponse.<List<EmailResponse>>builder()
                .code(1000)
                .result(emailService.getInboxs())
                .build();
    }

    @GetMapping("/sent")
    ApiResponse<List<EmailResponse>> getSentboxs(){
        log.info("Fetching sentbox emails");
        return ApiResponse.<List<EmailResponse>>builder()
                .code(1000)
                .result(emailService.getSentboxs())
                .build();
    }

    @GetMapping("/email/{id}")
    ApiResponse<EmailResponse> getMail(@PathVariable String id){
        log.info("Fetching mail with ID: {}", id);
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .result(emailService.getMail(id))
                .build();
    }

    @GetMapping("/email/inbox/{uid}")
    ApiResponse<EmailResponse> getInboxMail(@PathVariable String uid){
        log.info("Fetching inbox mail with UID: {}", uid);
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .result(emailService.getInboxMail(uid))
                .build();
    }

    @PostMapping("/search")
    ApiResponse<Page<EmailResponse>> search(@RequestBody SearchRequest searchRequest){
        log.info("Searching emails with query: {}", searchRequest.getQuery());
        return ApiResponse.<Page<EmailResponse>>builder()
                .code(1000)
                .result(emailService.search(searchRequest))
                .build();
    }
}

