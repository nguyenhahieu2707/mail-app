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
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(to)
                    .sub(sub)
                    .body(body)
                    .build();
            log.info("To: "+to);
            log.info("Sub: "+sub);
            log.info("body: "+body);
            log.info((attachment != null ? attachment.getOriginalFilename() : "null"));

            emailService.sendMail(emailRequest, attachment);

            return ApiResponse.builder()
                    .code(1000)
                    .message("Successfully!")
                    .build();

        } catch (Exception e) {
            return ApiResponse.builder()
                    .code(500)
                    .message("Error sending mail!: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/attachment")
    ResponseEntity<Resource> downloadAttachment (@RequestParam String path){
        return emailService.downloadAttachment(path);
    }

//    @GetMapping("/email/inbox/{uid}/attachment")
//    public ResponseEntity<InputStreamResource> downloadInboxAttachment(
//            @PathVariable String uid,
//            @RequestParam(required = false) String filename  // có thể không dùng
//    ) {
//        return emailService.streamInboxAttachment(uid, filename);
//    }

    @GetMapping(
            value = "/email/inbox/{uid}/attachment"
    )
    public ResponseEntity<?> downloadInboxAttachment(
            @PathVariable String uid,
            @RequestParam(required = false) String filename
    ) {
        return emailService.streamInboxAttachment(uid, filename);
    }


    @PostMapping("/createmail")
    ApiResponse<EmailResponse> createMail(@Valid @RequestBody EmailRequest emailRequest){
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .message("Create successfully!")
                .result(emailService.createMail(emailRequest))
                .build();
    }

    @GetMapping("/inbox")
    ApiResponse<List<EmailResponse>> getInboxs(){
        return ApiResponse.<List<EmailResponse>>builder()
                .code(1000)
                .result(emailService.getInboxs())
                .build();
    }

    @GetMapping("/sent")
    ApiResponse<List<EmailResponse>> getSentboxs(){
        return ApiResponse.<List<EmailResponse>>builder()
                .code(1000)
                .result(emailService.getSentboxs())
                .build();
    }

    @GetMapping("/email/{id}")
    ApiResponse<EmailResponse> getMail(@PathVariable String id){
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .result(emailService.getMail(id))
                .build();
    }

    @GetMapping("/email/inbox/{uid}")
    ApiResponse<EmailResponse> getInboxMail(@PathVariable String uid){
        return ApiResponse.<EmailResponse>builder()
                .code(1000)
                .result(emailService.getInboxMail(uid))
                .build();
    }

    @PostMapping("/search")
    ApiResponse<Page<EmailResponse>> search(@RequestBody SearchRequest searchRequest){
        return ApiResponse. <Page<EmailResponse>>builder()
                .code(1000)
                .result(emailService.search(searchRequest))
                .build();
    }
}
