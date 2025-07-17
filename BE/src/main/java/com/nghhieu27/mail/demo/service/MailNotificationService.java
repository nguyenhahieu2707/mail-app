package com.nghhieu27.mail.demo.service;

import com.nghhieu27.mail.demo.dto.request.MailNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

//    public void notifyNewMail(MailNotificationRequest mail) {
//        messagingTemplate.convertAndSend("/topic/mail", mail);
//    }

    public void notifyUserNewMail(String email, MailNotificationRequest mail) {
        log.info("📤 Sending mail to user [{}]", email);  // log email
        messagingTemplate.convertAndSendToUser(email, "/queue/mail", mail);
    }
}

