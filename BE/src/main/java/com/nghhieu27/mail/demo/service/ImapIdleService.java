package com.nghhieu27.mail.demo.service;

import com.nghhieu27.mail.demo.configuration.MailProperties;
import com.nghhieu27.mail.demo.dto.request.MailNotificationRequest;
import com.nghhieu27.mail.demo.entity.UserIMAP;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.InternetAddress;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImapIdleService {
    MailNotificationService mailNotificationService;
    MailProperties mailProperties;

    // Danh sách các listener đang chạy cho từng user
    final Map<String, ListenerState> userListeners = new HashMap<>();

    public synchronized void startListenerForUser(UserIMAP user) {
        if (userListeners.containsKey(user.getEmail())) {
            log.info("⚠️ Listener for {} already running", user.getEmail());
            return;
        }

        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", mailProperties.getProtocol());
            props.setProperty("mail.imap.host", mailProperties.getHost());
            props.setProperty("mail.imap.port", String.valueOf(mailProperties.getPort()));

            Session session = Session.getInstance(props);
            Store store = session.getStore(mailProperties.getProtocol());
            store.connect(user.getEmail(), mailProperties.getSharedPassword());

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            inbox.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent event) {
                    for (Message message : event.getMessages()) {
                        try {
                            String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                            String subject = message.getSubject();
                            log.info("📨 New mail for {} from {} - Subject: {}", user.getEmail(), from, subject);
                            mailNotificationService.notifyUserNewMail(
                                    user.getEmail(),
                                    new MailNotificationRequest(from, subject)
                            );
                        } catch (Exception e) {
                            log.error("❌ Error processing message for {}: {}", user.getEmail(), e.getMessage());
                        }
                    }
                }
            });

            Thread listenerThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            if (inbox instanceof IMAPFolder) {
                                ((IMAPFolder) inbox).idle();
                            } else {
                                Thread.sleep(10000);
                                inbox.getMessageCount();
                            }
                        } catch (InterruptedException e) {
                            log.info("🔕 Listener thread interrupted for {}", user.getEmail());
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ Listener error for {}: {}", user.getEmail(), e.getMessage());
                }
            }, "imap-listener-" + user.getEmail());

            listenerThread.start();
            userListeners.put(user.getEmail(), new ListenerState(listenerThread, store, inbox));

            log.info("✅ Listener started for {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to start listener for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public synchronized void stopListenerForUser(String email) {
        ListenerState state = userListeners.remove(email);
        if (state != null) {
            state.stop(email);
        } else {
            log.warn("⚠️ No running listener found for {}", email);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class ListenerState {
        Thread thread;
        Store store;
        Folder inbox;

        public ListenerState(Thread thread, Store store, Folder inbox) {
            this.thread = thread;
            this.store = store;
            this.inbox = inbox;
        }

        void stop(String email) {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close();
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
                log.info("🛑 Stopped listener for {}", email);
            } catch (Exception e) {
                log.error("⚠️ Error stopping listener for {}: {}", email, e.getMessage());
            }
        }
    }
}
