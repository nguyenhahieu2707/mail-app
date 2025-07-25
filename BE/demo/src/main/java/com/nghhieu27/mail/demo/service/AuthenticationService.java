package com.nghhieu27.mail.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import com.nghhieu27.mail.demo.Exception.AppException;
import com.nghhieu27.mail.demo.Exception.ErrorCode;
import com.nghhieu27.mail.demo.dto.request.*;
import com.nghhieu27.mail.demo.dto.response.AuthenticationResponse;
import com.nghhieu27.mail.demo.dto.response.IntrospectResponse;
import com.nghhieu27.mail.demo.entity.InvalidatedToken;
import com.nghhieu27.mail.demo.entity.User;
import com.nghhieu27.mail.demo.repository.InvalidatedTokenRepository;
import com.nghhieu27.mail.demo.repository.UserImapRepository;
import com.nghhieu27.mail.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    ImapIdleService imapIdleService;
    UserImapRepository userImapRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        log.info(token.toString());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        imapIdleService.startListenerForUser(userImapRepository.findByEmail(request.getEmail()).orElse(null));

        return AuthenticationResponse.builder().token(token).email(email).authenticated(true).build();
    }

    public AuthenticationResponse authenticate_LaoID(LaoIDRequest request) {
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
//        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
//        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        log.info("token  ");
        var token = generateToken(user);
        log.info(token.toString());

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("hieu.com")
                .claim("userId", user.getId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
//                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token!", e);
            throw new RuntimeException(e);
        }
    }

//    public void logout(LogoutRequest request) throws ParseException, JOSEException {
//        try {
//            imapIdleService.stopListenerForUser(SecurityContextHolder.getContext().getAuthentication().getName());
//
//            var signToken = verifyToken(request.getToken(), true);
//
//            String jit = signToken.getJWTClaimsSet().getJWTID();
//            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
//
//            InvalidatedToken invalidatedToken =
//                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
//
//            invalidatedTokenRepository.save(invalidatedToken);
//
//        } catch (AppException exception) {
//            log.info("Token already expired");
//        }
//    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            imapIdleService.stopListenerForUser(username);
            log.info("Username {}", username);

            var signToken = verifyToken(request.getToken(), true);
            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            // Tạo entity
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            // Cố gắng lưu, nếu duplicate thì bỏ qua
            try {
                invalidatedTokenRepository.save(invalidatedToken);
                log.info("✅ Token {} has been invalidated", jti);
            } catch (DataIntegrityViolationException ex) {
                log.info("ℹ️ Token {} already invalidated (duplicate caught)", jti);
            }

        } catch (AppException e) {
            log.info("⚠️ Token already expired or invalid");
        }
    }



    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException((ErrorCode.UNAUTHENTICATED));
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

//    private String buildScope(User user) {
//        StringJoiner stringJoiner = new StringJoiner(" ");
//        if (!CollectionUtils.isEmpty(user.getRoles()))
//            user.getRoles().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
//            });
//
//        return stringJoiner.toString();
//    }
}
