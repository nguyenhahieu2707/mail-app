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
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
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
import java.io.InputStream;
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
        String uploadDir = "/app/attachments/";

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

        return filePath.toString();
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

    public ResponseEntity<?> streamInboxAttachment(String uidStr, String filenameFilter) {
        Store store = null;
        Folder inbox = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            UIDFolder uf = (UIDFolder) inbox;
            Message msg = uf.getMessageByUID(Long.parseLong(uidStr));

            if (msg == null || !msg.isMimeType("multipart/*")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Không tìm thấy email hoặc không có multipart.");
            }

            Multipart multipart = (Multipart) msg.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fname = part.getFileName();
                    if (filenameFilter != null && !filenameFilter.equals(fname)) {
                        continue;
                    }

                    byte[] fileBytes = part.getInputStream().readAllBytes();
                    InputStreamResource resource = new InputStreamResource(new java.io.ByteArrayInputStream(fileBytes));

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fname + "\"")
                            .contentLength(fileBytes.length)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Không tìm thấy tệp đính kèm hợp lệ.");

        } catch (Exception e) {
            log.error("Lỗi khi xử lý tệp đính kèm", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Lỗi hệ thống khi xử lý tệp đính kèm: " + e.getMessage());

        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (MessagingException me) {
                log.warn("Không thể đóng kết nối IMAP", me);
            }
        }
    }


    public Page<EmailResponse> search(SearchRequest searchRequest){
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), Sort.by("date"));
        Page<Email> emailPage = emailRepository.advancedSearch(searchRequest.getQuery(), searchRequest.getFromDate(), searchRequest.getToDate(), searchRequest.isHasAttachment(), pageable);
        return emailPage.map(emailMapper::toEmailResponse);
    }

    public EmailResponse createMail(EmailRequest emailRequest){
        Email email = emailMapper.toEmail(emailRequest);

        return emailMapper.toEmailResponse(emailRepository.save(email));
    }

    public EmailResponse getMail(String id){
        return emailMapper.toEmailResponse(emailRepository.findById(id).orElseThrow());
    }

//    public EmailResponse getInboxMail(String uid){
//        Store store = null;
//        Folder inbox = null;
//
//        try {
//
//            System.out.println("protocol: " + mailProperties.getProtocol());
//            System.out.println("host: " + mailProperties.getHost());
//            System.out.println("port: " + mailProperties.getPort());
//
//            Properties props = new Properties();
//            props.put("mail.store.protocol", mailProperties.getProtocol());
//            props.put("mail.imap.host", mailProperties.getHost());
//            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
//            props.put("mail.imap.starttls.enable", "false");
//
//            Session session = Session.getDefaultInstance(props);
//            store = session.getStore(mailProperties.getProtocol());
//
//            String username = SecurityContextHolder.getContext().getAuthentication().getName();
//
//            store.connect(
//                    mailProperties.getHost(),
//                    username,
//                    mailProperties.getSharedPassword()
//            );
//
//            inbox = store.getFolder("INBOX");
//            inbox.open(Folder.READ_ONLY);
//
//            UIDFolder uf = (UIDFolder)inbox;
//            Long longUid = Long.parseLong(uid);
//            Message msg = uf.getMessageByUID(longUid);
//
//            EmailResponse emailResponse = new EmailResponse();
//            emailResponse.setFrom(((InternetAddress) msg.getFrom()[0]).toUnicodeString());
//            emailResponse.setSub(msg.getSubject());
//            //emailResponse.setBody(msg.getContent().toString());
//            emailResponse.setDate(msg.getSentDate());
//
//            // Xử lý body và attachment
//            if (msg.isMimeType("text/plain")) {
//                emailResponse.setBody(msg.getContent().toString());
//            } else if (msg.isMimeType("multipart/*")) {
//                Multipart mp = (Multipart) msg.getContent();
//                for (int i = 0; i < mp.getCount(); i++) {
//                    BodyPart part = mp.getBodyPart(i);
//
//                    // phần text
//                    if (part.isMimeType("text/plain") && emailResponse.getBody() == null) {
//                        emailResponse.setBody(part.getContent().toString());
//                    }
//
//                    // phần attachment
//                    String disp = part.getDisposition();
//                    if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
//                        String fname = part.getFileName();
//                        emailResponse.setAttachmentName(fname);
//                        // không cần lưu đường dẫn, vì ta stream trực tiếp qua endpoint GET
//                        break;
//                    }
//                }
//            }
//
//            return emailResponse;
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            try {
//                if (inbox != null && inbox.isOpen()) {
//                    inbox.close(false);
//                }
//                if (store != null && store.isConnected()) {
//                    store.close();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }

    public EmailResponse getInboxMail(String uid) {
        Store store = null;
        Folder inbox = null;

        try {
            log.info("protocol: {}", mailProperties.getProtocol());
            log.info("host: {}", mailProperties.getHost());
            log.info("port: {}", mailProperties.getPort());

            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            UIDFolder uf = (UIDFolder) inbox;
            Message msg = uf.getMessageByUID(Long.parseLong(uid));
            if (msg == null) throw new RuntimeException("Không tìm thấy thư");

            EmailResponse emailResponse = new EmailResponse();
            emailResponse.setFrom(((InternetAddress) msg.getFrom()[0]).toUnicodeString());
            emailResponse.setSub(msg.getSubject());
            emailResponse.setDate(msg.getSentDate());

            // Xử lý body (plain/html/đệ quy)
            String bodyText = extractTextFromMessage(msg);
            emailResponse.setBody(bodyText);

            // Xử lý attachment
            if (msg.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) msg.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart part = mp.getBodyPart(i);
                    String disp = part.getDisposition();
                    if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                        emailResponse.setAttachmentName(part.getFileName());
                        break;
                    }
                }
            }

            return emailResponse;

        } catch (Exception e) {
            log.error("Lỗi khi đọc mail inbox", e);
            throw new RuntimeException("Không thể đọc mail inbox", e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception e) {
                log.warn("Lỗi khi đóng kết nối mail", e);
            }
        }
    }

    private String extractTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();
            return extractTextFromMultipart(mp);
        }
        return "";
    }

    private String extractTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                continue; // skip attachment
            }

            if (part.isMimeType("text/html")) {
                return part.getContent().toString();
            } else if (part.isMimeType("text/plain")) {
                return part.getContent().toString(); // fallback
            } else if (part.isMimeType("multipart/*")) {
                return extractTextFromMultipart((Multipart) part.getContent());
            }
        }
        return "";
    }


    public List<EmailResponse> getSentboxs(){
        String from = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Email> emails = emailRepository.findByFrom(from).orElse(Collections.emptyList());
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
//            System.out.println("username: " + mailProperties.getUsername());
//            System.out.println("password: " + mailProperties.getPassword());

            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            store.connect(
                    mailProperties.getHost(),
                    username,
                    mailProperties.getSharedPassword()
            );

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            UIDFolder uf = (UIDFolder) inbox;

            Message[] messages = inbox.getMessages();
            for (int i = messages.length - 1; i >= Math.max(0, messages.length - 10); i--) {
                Message message = messages[i];
                Email email = new Email();

                long uid = uf.getUID(message);
                email.setId(String.valueOf(uid));

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
