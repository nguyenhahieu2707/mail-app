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
        log.info("Introspected token [{}]: valid = {}", token.substring(0, 10) + "...", isValid);
        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            log.warn("Invalid credentials for user: {}", request.getEmail());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);
        log.info("Token generated for user: {}", user.getEmail());

        imapIdleService.startListenerForUser(userImapRepository.findByEmail(user.getEmail()).orElse(null));
        log.info("Started IMAP listener for user: {}", user.getEmail());

        return AuthenticationResponse.builder()
                .token(token)
                .email(user.getEmail())
                .authenticated(true)
                .build();
    }

    public AuthenticationResponse authenticate_LaoID(LaoIDRequest request) {
        log.info("Authenticating LaoID user: {}", request.getEmail());

        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found (LaoID): {}", request.getEmail());
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });

        var token = generateToken(user);
        log.info("Token generated for LaoID user: {}", user.getEmail());

        imapIdleService.startListenerForUser(userImapRepository.findByEmail(user.getEmail()).orElse(null));
        log.info("Started IMAP listener for LaoID user: {}", user.getEmail());

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String generateToken(User user) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer("hieu.com")
                    .claim("userId", user.getId())
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claims.toJSONObject()));
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to generate JWT token", e);
            throw new RuntimeException("JWT generation failed", e);
        }
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Logging out user: {}", username);

            imapIdleService.stopListenerForUser(username);
            log.info("Stopped IMAP listener for user: {}", username);

            var signedToken = verifyToken(request.getToken(), true);
            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            try {
                invalidatedTokenRepository.save(
                        InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build()
                );
                log.info("Token {} invalidated successfully", jti);
            } catch (DataIntegrityViolationException ex) {
                log.info("Token {} already invalidated", jti);
            }

        } catch (AppException e) {
            log.warn("Logout failed: token already expired or invalid");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var email = signedJWT.getJWTClaimsSet().getSubject();

        invalidatedTokenRepository.save(InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build());
        log.info("Refreshing token for user: {}", email);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? Date.from(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS))
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!signedJWT.verify(new MACVerifier(SIGNER_KEY.getBytes())) || expiryTime.before(new Date())) {
            log.warn("Invalid or expired token");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        if (invalidatedTokenRepository.existsById(jti)) {
            log.warn("Token {} has been invalidated", jti);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}

