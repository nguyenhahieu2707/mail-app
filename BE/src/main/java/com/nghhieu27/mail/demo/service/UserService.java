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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserImapRepository userImapRepository;
    UserMapper userMapper;
    MailProperties mailProperties;

    public UserResponse createUser(UserCreationRequest request) {
        log.info("Creating user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User already exists: {}", request.getEmail());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        String dovecotPassword = Crypt.crypt(
                mailProperties.getSharedPassword(), "$6$" + UUID.randomUUID().toString().substring(0, 8));

        UserIMAP userIMAP = new UserIMAP();
        userIMAP.setEmail(user.getEmail());
        userIMAP.setPassword(dovecotPassword);
        userImapRepository.save(userIMAP);
        log.info("Saved IMAP account for user: {}", user.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var savedUser = userRepository.save(user);
        log.info("User saved successfully: {}", savedUser.getEmail());

        return userMapper.toUserResponse(savedUser);
    }

    public List<UserResponse> getUsers() {
        log.info("Fetching all users");
        return userMapper.toListUserResponse(userRepository.findAll());
    }

    public UserResponse getUser(String userId) {
        log.info("Fetching user with ID: {}", userId);
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new RuntimeException("User not found!");
                })
        );
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
    }
}



