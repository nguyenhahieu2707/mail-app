package com.nghhieu27.mail.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "from_email")
    String from;

    @Column(name = "to_email")
    String to;

    String sub;
    String body;

    Date date;

    @Column(name = "attachment_name")
    String attachmentName;

    @Column(name = "attachment_path")
    String attachmentPath;
}
