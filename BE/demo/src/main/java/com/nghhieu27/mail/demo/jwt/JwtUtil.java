package com.nghhieu27.mail.demo.jwt;

import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.signerKey}")
    private String signerKey;

    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            log.info(signedJWT.getJWTClaimsSet().getSubject());
            return signedJWT.getJWTClaimsSet().getSubject();  // "sub" chính là email
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}


