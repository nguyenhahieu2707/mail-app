package com.nghhieu27.mail.demo.mapper;

import com.nghhieu27.mail.demo.dto.request.UserCreationRequest;
import com.nghhieu27.mail.demo.dto.response.UserResponse;
import com.nghhieu27.mail.demo.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T13:00:34+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );
        user.password( request.getPassword() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.email( user.getEmail() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );

        return userResponse.build();
    }

    @Override
    public List<UserResponse> toListUserResponse(List<User> lst) {
        if ( lst == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( lst.size() );
        for ( User user : lst ) {
            list.add( toUserResponse( user ) );
        }

        return list;
    }
}
