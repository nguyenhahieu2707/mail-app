package com.nghhieu27.mail.demo.listener;

import com.nghhieu27.mail.demo.dto.request.MailNotificationRequest;
import com.nghhieu27.mail.demo.service.MailNotificationService;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

@Slf4j
@Component
public class ImapIdleListener {
    @Autowired
    private MailNotificationService mailNotificationService;

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port}")
    private int port;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.protocol}")
    private String protocol;

    @PostConstruct
    public void listenForMail() {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.setProperty("mail.store.protocol", protocol);
                props.setProperty("mail.imap.host", host);
                props.setProperty("mail.imap.port", String.valueOf(port));
                props.setProperty("mail.imap.starttls.enable", "false");

                Session session = Session.getInstance(props);
                Store store = session.getStore(protocol);
                store.connect(host, username, password);

                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                inbox.addMessageCountListener(new MessageCountAdapter() {
                    @Override
                    public void messagesAdded(MessageCountEvent event) {
                        for (Message message : event.getMessages()) {
                            try {
                                String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                                String subject = message.getSubject();
                                log.info("📨 New mail from {} - Subject: {}", from, subject);
                                mailNotificationService.notifyNewMail(new MailNotificationRequest(from, subject));
                            } catch (Exception e) {
                                log.error("Failed to process incoming message", e);
                            }
                        }
                    }
                });

                log.info("✅ IMAP IDLE listener started. Waiting for new emails...");
                while (true) {
                    if (inbox instanceof com.sun.mail.imap.IMAPFolder) {
                        ((com.sun.mail.imap.IMAPFolder) inbox).idle();
                    } else {
                        // fallback polling (if not IMAP)
                        Thread.sleep(10000);
                        inbox.getMessageCount();
                    }
                }

            } catch (Exception ex) {
                log.error("💥 Error in IMAP listener", ex);
            }
        }, "imap-idle-thread").start();
    }
}
