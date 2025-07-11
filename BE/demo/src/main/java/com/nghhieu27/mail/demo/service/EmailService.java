package com.nghhieu27.mail.demo.service;

import com.google.protobuf.Api;
import com.nghhieu27.mail.demo.Exception.AppException;
import com.nghhieu27.mail.demo.Exception.ErrorCode;
import com.nghhieu27.mail.demo.configuration.MailProperties;
import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.request.SearchRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.entity.Email;
import com.nghhieu27.mail.demo.mapper.EmailMapper;
import com.nghhieu27.mail.demo.repository.EmailRepository;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
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

    public void sendMail(EmailRequest emailRequest, MultipartFile attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper;
            String path_file = null;
            String name_file = null;

            boolean multipart = attachment != null && !attachment.isEmpty();
            helper = new MimeMessageHelper(message, multipart);

            String from = SecurityContextHolder.getContext().getAuthentication().getName();
            helper.setFrom(from);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSub());
            helper.setText(emailRequest.getBody(), false); // false = plain text

            if (multipart) {
                byte[] fileBytes = attachment.getBytes(); // Đọc trước
                name_file = attachment.getOriginalFilename();
                log.info(name_file);
                path_file = saveAttachment(attachment);   // Sau đó lưu

                helper.addAttachment(
                        attachment.getOriginalFilename(),
                        new ByteArrayResource(fileBytes)       // Dùng lại mảng byte đã đọc
                );
            }


            Email email = emailMapper.toEmail(emailRequest);
            email.setFrom(from);
            email.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            if(path_file!=null){
                email.setAttachmentPath(path_file);
                email.setAttachmentName(name_file);
            }
            emailRepository.save(email);
            log.info("Final Email: " + email.toString());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String saveAttachment(MultipartFile file) throws IOException {
        String uploadDir = "D:/attachments/";

        // Đảm bảo thư mục tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file ngẫu nhiên, tránh trùng
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Tạo path đầy đủ
        Path filePath = uploadPath.resolve(fileName);

        // Lưu file
        file.transferTo(filePath.toFile());

        System.out.println("File saved: " + filePath.toAbsolutePath());

        return filePath.toString(); // ✅ Trả về path để lưu vào DB
    }

    public ResponseEntity<Resource> downloadAttachment(String path){
        try {
            Path filePath = Paths.get(path);
            Resource file = new UrlResource(filePath.toUri());

            if (!file.exists() || !file.isReadable()) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);
        } catch (Exception e) {
            log.error("Error downloading attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    public void sendMail(EmailRequest emailRequest){
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//
//        String fr = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        simpleMailMessage.setFrom(fr);
//        simpleMailMessage.setTo(emailRequest.getTo());
//        simpleMailMessage.setSubject(emailRequest.getSub());
//        simpleMailMessage.setText(emailRequest.getBody());
//
//        Email email = emailMapper.toEmail(emailRequest);
//        email.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
//
//        emailRepository.save(email);
//
//        javaMailSender.send(simpleMailMessage);
//    }
    public Page<EmailResponse> search(SearchRequest searchRequest){
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), Sort.by("date"));
        Page<Email> emailPage = emailRepository.searchBySubOrBody(searchRequest.getQuery(), pageable);
        return emailPage.map(emailMapper::toEmailResponse);
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
