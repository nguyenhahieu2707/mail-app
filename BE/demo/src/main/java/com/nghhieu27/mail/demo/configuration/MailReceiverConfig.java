package com.nghhieu27.mail.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mail.ImapMailReceiver;

@Configuration
public class MailReceiverConfig {

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port}")
    private int port;

    @Bean
    public ImapMailReceiver imapMailReceiver() {
        String imapUrl = String.format("imap://%s:%s@%s:%d/INBOX", username, password, host, port);
        ImapMailReceiver receiver = new ImapMailReceiver(imapUrl);
        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);
        receiver.setAutoCloseFolder(false);
        receiver.setSimpleContent(true); // chỉ lấy nội dung đơn giản (text)
        return receiver;
    }
}

