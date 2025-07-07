package com.nghhieu27.mail.demo.controller;

import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.service.EmailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mail")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailController {
    @Autowired
    EmailService emailService;

    @PostMapping("/sendmail")
    ApiResponse<?> sendmail(@Valid @RequestBody EmailRequest emailRequest){
        try {
            emailService.sendMail(emailRequest);
            return ApiResponse.builder()
                    .code(1000)
                    .message("Successfully!")
                    .build();
        } catch (Exception e) {
            return ApiResponse.builder()
                    .code(500)
                    .message("Error sending mail!: "+e.getMessage())
                    .build();
        }
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
}
