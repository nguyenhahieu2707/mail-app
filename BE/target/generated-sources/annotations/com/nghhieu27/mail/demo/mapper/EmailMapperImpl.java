package com.nghhieu27.mail.demo.mapper;

import com.nghhieu27.mail.demo.dto.request.EmailRequest;
import com.nghhieu27.mail.demo.dto.response.EmailResponse;
import com.nghhieu27.mail.demo.entity.Email;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T13:00:33+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class EmailMapperImpl implements EmailMapper {

    @Override
    public Email toEmail(EmailRequest request) {
        if ( request == null ) {
            return null;
        }

        Email.EmailBuilder email = Email.builder();

        email.from( request.getFrom() );
        email.to( request.getTo() );
        email.sub( request.getSub() );
        email.body( request.getBody() );

        return email.build();
    }

    @Override
    public EmailResponse toEmailResponse(Email email) {
        if ( email == null ) {
            return null;
        }

        EmailResponse.EmailResponseBuilder emailResponse = EmailResponse.builder();

        emailResponse.id( email.getId() );
        emailResponse.from( email.getFrom() );
        emailResponse.to( email.getTo() );
        emailResponse.sub( email.getSub() );
        emailResponse.body( email.getBody() );
        emailResponse.date( email.getDate() );
        emailResponse.attachmentName( email.getAttachmentName() );
        emailResponse.attachmentPath( email.getAttachmentPath() );
        emailResponse.type( email.getType() );

        return emailResponse.build();
    }

    @Override
    public List<EmailResponse> toListEmailResponse(List<Email> lst) {
        if ( lst == null ) {
            return null;
        }

        List<EmailResponse> list = new ArrayList<EmailResponse>( lst.size() );
        for ( Email email : lst ) {
            list.add( toEmailResponse( email ) );
        }

        return list;
    }
}
