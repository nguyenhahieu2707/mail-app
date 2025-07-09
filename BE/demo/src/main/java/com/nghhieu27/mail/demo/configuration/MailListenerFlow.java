package com.nghhieu27.mail.demo.configuration;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;

@Configuration
public class MailListenerFlow {

    @Bean
    public IntegrationFlow mailListenerFlow(ImapMailReceiver imapMailReceiver) {
        return IntegrationFlows
                .from(Mail.imapIdleAdapter(imapMailReceiver)
                                .autoStartup(true)
                                .shouldReconnectAutomatically(true),
                        e -> e.poller(Pollers.fixedDelay(10000)))
                .handle(message -> {
                    MimeMessage msg = (MimeMessage) message.getPayload();
                    try {
                        System.out.println("📬 Email mới: " + msg.getSubject());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }
}
