package com.nghhieu27.mail.demo.controller;

import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.LogFeRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/log")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogFeController {
    Logger logger = LoggerFactory.getLogger("FrontendLogger");

    @PostMapping
    ApiResponse<?> receiveFeLog (@RequestBody LogFeRequest request){
        String level = request.getLevel() != null ? request.getLevel().toUpperCase() : "INFO";
        String message = request.getMessage();

        switch (level) {
            case "DEBUG" -> logger.debug(message);
            case "INFO" -> logger.info(message);
            case "WARN" -> logger.warn(message);
            case "ERROR" -> logger.error(message);
            default -> logger.info("[UNKNOWN LEVEL] " + message);
        }

        return ApiResponse.builder()
                .code(1000)
                .build();
    }
}
