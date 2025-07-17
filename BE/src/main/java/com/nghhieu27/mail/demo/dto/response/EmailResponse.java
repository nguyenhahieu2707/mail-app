package com.nghhieu27.mail.demo.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {

    String id;
    String from;
    String to;
    String sub;
    String body;
    Date date;
    String attachmentName;
    String attachmentPath;
}
