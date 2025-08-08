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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.nghhieu27.mail.demo.enums.Type;

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
        String to = emailRequest.getTo();
        log.info("Attempting to send an email to: {}", to);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper;
            String path_file = null;
            String name_file = null;

            boolean multipart = attachment != null && !attachment.isEmpty();
            helper = new MimeMessageHelper(message, multipart);

            String from = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Logged in user (from): {}", from);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(emailRequest.getSub());
            helper.setText(emailRequest.getBody(), false); // false = plain text

            if (multipart) {
                log.info("Processing attachment: {}", attachment.getOriginalFilename());
                byte[] fileBytes = attachment.getBytes();
                name_file = attachment.getOriginalFilename();
                path_file = saveAttachment(attachment);

                helper.addAttachment(
                        attachment.getOriginalFilename(),
                        new ByteArrayResource(fileBytes)
                );
                log.info("Attachment '{}' added successfully.", name_file);
            }

            Email email = emailMapper.toEmail(emailRequest);
            email.setFrom(from);
            email.setType(Type.SENT);
            log.info("Email type: {}", email.getType());
            email.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            if (path_file != null) {
                email.setAttachmentPath(path_file);
                email.setAttachmentName(name_file);
            }

            Email savedEmail = emailRepository.save(email);
            log.info("Email record saved to database with ID: {}", savedEmail.getId());

            javaMailSender.send(message);
            log.info("Successfully sent email to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String saveAttachment(MultipartFile file) throws IOException {
        log.info("Attempting to save attachment: {}", file.getOriginalFilename());
        String uploadDir = "/app/attachments/";

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            log.warn("Upload directory does not exist. Creating directory: {}", uploadDir);
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        log.info("File saved successfully at path: {}", filePath);
        return filePath.toString();
    }

    public ResponseEntity<Resource> downloadAttachment(String path) {
        log.info("Attempting to download attachment from path: {}", path);
        try {
            Path filePath = Paths.get(path);
            Resource file = new UrlResource(filePath.toUri());

            if (!file.exists() || !file.isReadable()) {
                log.error("Attachment not found or not readable at path: {}", path);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            String filename = filePath.getFileName().toString();
            log.info("Attachment '{}' found. Preparing for download.", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);
        } catch (Exception e) {
            log.error("Error downloading attachment from path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> streamInboxAttachment(String uidStr, String filenameFilter) {
        log.info("Attempting to stream attachment from inbox email UID: {} with filter: '{}'", uidStr, filenameFilter);
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
            log.info("Connecting to IMAP server {}:{} for user {}", mailProperties.getHost(), mailProperties.getPort(), username);
            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            UIDFolder uf = (UIDFolder) inbox;
            Message msg = uf.getMessageByUID(Long.parseLong(uidStr));

            if (msg == null) {
                log.warn("Email with UID: {} not found.", uidStr);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy email.");
            }
            if(!msg.isMimeType("multipart/*")){
                log.warn("Email with UID: {} is not multipart.", uidStr);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không chứa tệp đính kèm.");
            }

            Multipart multipart = (Multipart) msg.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fname = part.getFileName();
                    if (filenameFilter != null && !filenameFilter.equals(fname)) {
                        continue;
                    }
                    log.info("Found attachment '{}'. Preparing to stream.", fname);
                    byte[] fileBytes = part.getInputStream().readAllBytes();
                    InputStreamResource resource = new InputStreamResource(new java.io.ByteArrayInputStream(fileBytes));

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fname + "\"")
                            .contentLength(fileBytes.length)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }
            }

            log.warn("No matching attachment found for UID: {} with filter: {}", uidStr, filenameFilter);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Không tìm thấy tệp đính kèm hợp lệ.");

        } catch (Exception e) {
            log.error("Error streaming attachment for UID: {}", uidStr, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Lỗi hệ thống khi xử lý tệp đính kèm: " + e.getMessage());

        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
                log.info("IMAP connection closed for attachment streaming.");
            } catch (MessagingException me) {
                log.warn("Could not close IMAP connection cleanly.", me);
            }
        }
    }

    public Page<EmailResponse> search(SearchRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("date").descending());
        List<Email> combined = new ArrayList<>();

        // 1. Email đã gửi trong DB
        if(request.getType().equals(Type.SENT)||request.getType().equals(Type.ALL)) {
            Page<Email> sentPage = emailRepository.advancedSearch(
                    request.getQuery(),
                    username,
                    request.getFromDate(),
                    request.getToDate(),
                    request.isHasAttachment(),
                    Pageable.unpaged()
            );
            combined.addAll(sentPage.getContent());
        }

        // 2. Email đã nhận trong Dovecot
        if(request.getType().equals(Type.INBOX)||request.getType().equals(Type.ALL)) {
            List<Email> received = searchInboxViaIMAP(request, username);
            combined.addAll(received);
        }

        // 3. Gộp và sắp xếp theo ngày gửi mới nhất
        combined.sort(Comparator.comparing(Email::getDate).reversed());

        // 4. Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        List<EmailResponse> pagedContent = combined.subList(start, end).stream()
                .map(emailMapper::toEmailResponse)
                .toList();

        return new PageImpl<>(pagedContent, pageable, combined.size());
    }


    private List<Email> searchInboxViaIMAP(SearchRequest request, String username) {
        List<Email> result = new ArrayList<>();

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore(mailProperties.getProtocol());
            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {
                String body = extractTextFromMessage(msg);
                String subject = msg.getSubject();
                String from = ((InternetAddress) msg.getFrom()[0]).getAddress();
                Date sentDate = msg.getSentDate();

                String keyword = Optional.ofNullable(request.getQuery()).orElse("").toLowerCase();
                if (!keyword.isEmpty() &&
                        (subject == null || !subject.toLowerCase().contains(keyword)) &&
                        (body == null || !body.toLowerCase().contains(keyword))) {
                    continue;
                }

                // Filter theo date
                if (request.getFromDate() != null && sentDate.before(request.getFromDate())) continue;
                if (request.getToDate() != null && sentDate.after(request.getToDate())) continue;

                // Filter hasAttachment
                boolean hasAttachment = false;
                if (msg.isMimeType("multipart/*")) {
                    Multipart mp = (Multipart) msg.getContent();
                    for (int i = 0; i < mp.getCount(); i++) {
                        BodyPart part = mp.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            hasAttachment = true;
                            break;
                        }
                    }
                }
                if (request.isHasAttachment() && !hasAttachment) continue;

                Email email = new Email();
                UIDFolder uf = (UIDFolder) inbox;
                email.setId(String.valueOf(uf.getUID(msg)));
                email.setFrom(from);
                email.setTo(username);
                email.setSub(subject);
                email.setBody(body);
                email.setDate(sentDate);
                email.setType(Type.INBOX);
                if (hasAttachment) email.setAttachmentName("Có đính kèm"); // placeholder
                result.add(email);
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Failed to search inbox via IMAP", e);
        }

        return result;
    }


    public EmailResponse createMail(EmailRequest emailRequest) {
        log.info("Creating a new email record (draft) for recipient: {}", emailRequest.getTo());
        Email email = emailMapper.toEmail(emailRequest);
        Email savedEmail = emailRepository.save(email);
        log.info("Email record created with ID: {}", savedEmail.getId());
        return emailMapper.toEmailResponse(savedEmail);
    }

    public EmailResponse getMail(String id) {
        log.info("Fetching email from database with ID: {}", id);
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Email with ID: {} not found in database.", id);
                    return new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Hoặc một lỗi cụ thể hơn
                });
        return emailMapper.toEmailResponse(email);
    }

    public EmailResponse getInboxMail(String uid) {
        log.info("Fetching details for inbox email with UID: {}", uid);
        Store store = null;
        Folder inbox = null;

        try {
            log.info("Connecting to IMAP server {}:{} with protocol {}", mailProperties.getHost(), mailProperties.getPort(), mailProperties.getProtocol());

            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());
            log.info("IMAP connection successful for user: {}", username);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            UIDFolder uf = (UIDFolder) inbox;
            Message msg = uf.getMessageByUID(Long.parseLong(uid));
            if (msg == null) {
                log.error("Email with UID {} not found in INBOX.", uid);
                throw new RuntimeException("Không tìm thấy thư");
            }
            log.info("Successfully fetched message for UID: {}", uid);

            EmailResponse emailResponse = new EmailResponse();
            emailResponse.setFrom(((InternetAddress) msg.getFrom()[0]).toUnicodeString());
            emailResponse.setSub(msg.getSubject());
            emailResponse.setDate(msg.getSentDate());

            String bodyText = extractTextFromMessage(msg);
            emailResponse.setBody(bodyText);

            if (msg.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) msg.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart part = mp.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        emailResponse.setAttachmentName(part.getFileName());
                        log.info("Found attachment in email UID {}: {}", uid, part.getFileName());
                        break;
                    }
                }
            }

            return emailResponse;

        } catch (Exception e) {
            log.error("Failed to read inbox email with UID: {}", uid, e);
            throw new RuntimeException("Không thể đọc mail inbox", e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
                log.info("IMAP connection closed for getInboxMail.");
            } catch (Exception e) {
                log.warn("Error closing IMAP resources for getInboxMail.", e);
            }
        }
    }

    private String extractTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return extractTextFromMultipart((Multipart) message.getContent());
        }
        return "";
    }

    private String extractTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                continue;
            }
            if (part.isMimeType("text/html") || part.isMimeType("text/plain")) {
                return part.getContent().toString();
            } else if (part.isMimeType("multipart/*")) {
                String content = extractTextFromMultipart((Multipart) part.getContent());
                if (!content.isEmpty()) {
                    return content;
                }
            }
        }
        return "";
    }

    public List<EmailResponse> getSentboxs() {
        String from = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching sentbox for user: {}", from);
        List<Email> emails = emailRepository.findByFrom(from).orElse(Collections.emptyList());
        log.info("Found {} emails in sentbox for user: {}", emails.size(), from);
        return emailMapper.toListEmailResponse(emails);
    }

    public List<EmailResponse> getInboxs() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching latest 10 inbox emails for user: {}", username);
        List<EmailResponse> inboxs = new ArrayList<>();
        Store store = null;
        Folder inbox = null;

        try {
            log.info("Connecting to IMAP server {}:{} with protocol {}", mailProperties.getHost(), mailProperties.getPort(), mailProperties.getProtocol());
            Properties props = new Properties();
            props.put("mail.store.protocol", mailProperties.getProtocol());
            props.put("mail.imap.host", mailProperties.getHost());
            props.put("mail.imap.port", String.valueOf(mailProperties.getPort()));
            props.put("mail.imap.starttls.enable", "false");

            Session session = Session.getDefaultInstance(props);
            store = session.getStore(mailProperties.getProtocol());

            store.connect(mailProperties.getHost(), username, mailProperties.getSharedPassword());
            log.info("IMAP connection successful for user: {}", username);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            UIDFolder uf = (UIDFolder) inbox;
            Message[] messages = inbox.getMessages();
            int start = Math.max(0, messages.length - 10);
            log.info("Found {} total messages in INBOX. Processing from index {}.", messages.length, start);

            for (int i = messages.length - 1; i >= start; i--) {
                Message message = messages[i];
                Email email = new Email();

                email.setId(String.valueOf(uf.getUID(message)));
                Address[] fromAddresses = message.getFrom();
                email.setFrom( (fromAddresses != null && fromAddresses.length > 0) ? fromAddresses[0].toString() : "unknown");
                email.setSub(message.getSubject());
                email.setBody(message.getContent().toString());
                email.setDate(message.getSentDate());
                inboxs.add(emailMapper.toEmailResponse(email));
            }
            log.info("Successfully processed {} emails from inbox.", inboxs.size());
            return inboxs;
        } catch (Exception e) {
            log.error("Failed to fetch inbox for user: {}", username, e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
                log.info("IMAP connection closed for getInboxs.");
            } catch (Exception ex) {
                log.warn("Could not close IMAP resources cleanly after fetching inbox.", ex);
            }
        }
    }
}