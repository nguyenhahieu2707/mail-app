package com.nghhieu27.mail.demo.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mail.imap")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailProperties {
    String host;
    int port;
    String protocol;
    String username;
    String password;
}
