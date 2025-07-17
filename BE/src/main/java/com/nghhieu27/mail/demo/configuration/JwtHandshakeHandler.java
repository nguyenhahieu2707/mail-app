package com.nghhieu27.mail.demo.configuration;

import com.nghhieu27.mail.demo.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtUtil jwtUtil;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String query = request.getURI().getQuery(); // token=PWi...
        String token = null;

        if (query != null && query.startsWith("token=")) {
            token = query.substring("token=".length());
        }

        if (token == null) {
            log.warn("❌ No token found in WebSocket handshake request");
            return null;
        }

        try {
            String email = jwtUtil.getEmailFromToken(token);
            log.info("✅ WebSocket connected for user: {}", email);
            return () -> email;
        } catch (Exception e) {
            log.error("❌ Invalid token in WebSocket handshake: {}", e.getMessage());
            return null;
        }
    }
}
