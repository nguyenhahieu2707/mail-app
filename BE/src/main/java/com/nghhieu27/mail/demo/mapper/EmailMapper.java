package com.nghhieu27.mail.demo.mapper;

import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.entity.Email;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    Email toEmail(EmailRequest request);

    EmailResponse toEmailResponse(Email email);

    List<EmailResponse> toListEmailResponse(List<Email> lst);

}
