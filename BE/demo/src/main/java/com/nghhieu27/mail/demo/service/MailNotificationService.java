package com.nghhieu27.mail.demo.service;

import com.nghhieu27.mail.demo.dto.request.MailNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewMail(MailNotificationRequest mail) {
        messagingTemplate.convertAndSend("/topic/mail", mail);
    }
}

