package com.nghhieu27.mail.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    String from;

    @Email(message = "Please enter correct email format!")
    String to;

    @NotNull(message = "Title cannot be blank!")
    String sub;

    String body;
}
