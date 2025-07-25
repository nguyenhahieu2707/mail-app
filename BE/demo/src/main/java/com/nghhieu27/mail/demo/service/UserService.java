package com.nghhieu27.mail.demo.service;

import com.nghhieu27.mail.demo.Exception.AppException;
import com.nghhieu27.mail.demo.Exception.ErrorCode;
import com.nghhieu27.mail.demo.configuration.MailProperties;
import com.nghhieu27.mail.demo.dto.request.UserCreationRequest;
import com.nghhieu27.mail.demo.dto.response.UserResponse;
import com.nghhieu27.mail.demo.entity.User;
import com.nghhieu27.mail.demo.entity.UserIMAP;
import com.nghhieu27.mail.demo.mapper.UserMapper;
import com.nghhieu27.mail.demo.repository.UserImapRepository;
import com.nghhieu27.mail.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserImapRepository userImapRepository;
    UserMapper userMapper;
    MailProperties mailProperties;
//    PasswordEncoder passwordEncoder;


    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        String dovecotPassword = Crypt.crypt(mailProperties.getSharedPassword(), "$6$" + UUID.randomUUID().toString().substring(0, 8));

        UserIMAP userIMAP = new UserIMAP();
        userIMAP.setEmail(user.getEmail());
        userIMAP.setPassword(dovecotPassword);

        userImapRepository.save(userIMAP);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

//        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

//    @PreAuthorize("hasRole('ADMIN')")
    //   @PreAuthorize("hasAuthority('UPDATE_DATA')")
    public List<UserResponse> getUsers() {
        return userMapper.toListUserResponse(userRepository.findAll());
    }

//    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!")));
    }

//    public UserResponse updateUser(String userId, UserUpdateRequest request) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
//
//        userMapper.updateUser(user, request);
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        var roles = roleRepository.findAllById(request.getRoles());
//        user.setRoles(new HashSet<>(roles));
//
//        return userMapper.toUserResponse(userRepository.save(user));
//    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}


