package com.nghhieu27.mail.demo.service;

import com.nghhieu27.mail.demo.configuration.MailProperties;
import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.entity.Email;
import com.nghhieu27.mail.demo.mapper.EmailMapper;
import com.nghhieu27.mail.demo.repository.EmailRepository;
import jakarta.mail.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailService {
    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    EmailMapper emailMapper;

    @Autowired
    EmailRepository emailRepository;

    @Autowired
    MailProperties mailProperties;

    public void sendMail(EmailRequest emailRequest){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("admin@localhost");
        simpleMailMessage.setTo(emailRequest.getTo());
        simpleMailMessage.setSubject(emailRequest.getSub());
        simpleMailMessage.setText(emailRequest.getBody());

        Email email = emailMapper.toEmail(emailRequest);
        email.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        emailRepository.save(email);

        javaMailSender.send(simpleMailMessage);
    }

    public EmailResponse createMail(EmailRequest emailRequest){
        Email email = emailMapper.toEmail(emailRequest);

        return emailMapper.toEmailResponse(emailRepository.save(email));
    }

    public EmailResponse getMail(String id){
        return emailMapper.toEmailResponse(emailRepository.findById(id).orElseThrow());
    }

    public List<EmailResponse> getSentboxs(){
        List<Email> emails = emailRepository.findAll();
        return emailMapper.toListEmailResponse(emails);
    }

    public List<EmailResponse> getInboxs(){
        List<EmailResponse> inboxs = new ArrayList<>();
        Store store = null;
        Folder inbox = null;

        try {

            System.out.println("protocol: " + mailProperties.getProtocol());
            System.out.println("host: " + mailProperties.getHost());
            System.out.println("port: " + mailProperties.getPort());
            System.out.println("username: " + mailProperties.getUsername());
            System.out.println("password: " + mailProperties.getPassword());

            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());
            store.connect(
                    mailProperties.getHost(),
                    mailProperties.getUsername(),
                    mailProperties.getPassword()
            );

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (int i = messages.length - 1; i >= Math.max(0, messages.length - 10); i--) {
                Message message = messages[i];
                Email email = new Email();

                Address[] fromAddresses = message.getFrom();
                if (fromAddresses != null && fromAddresses.length > 0) {
                    email.setFrom(fromAddresses[0].toString());
                } else {
                    email.setFrom("unknown");
                }

                email.setSub(message.getSubject());
                email.setBody(message.getContent().toString());
                email.setDate(message.getSentDate());
                inboxs.add(emailMapper.toEmailResponse(email));
            }
            return inboxs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false); // đóng folder
                }
                if (store != null && store.isConnected()) {
                    store.close(); // đóng kết nối IMAP
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
