package com.nghhieu27.mail.demo.controller;

import com.nghhieu27.mail.demo.Exception.AppException;
import com.nghhieu27.mail.demo.Exception.ErrorCode;
import com.nghhieu27.mail.demo.configuration.MailProperties;
import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.LaoIDRequest;
import com.nghhieu27.mail.demo.dto.response.AuthenticationResponse;
import com.nghhieu27.mail.demo.entity.User;
import com.nghhieu27.mail.demo.entity.UserIMAP;
import com.nghhieu27.mail.demo.repository.UserImapRepository;
import com.nghhieu27.mail.demo.repository.UserRepository;
import com.nghhieu27.mail.demo.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/laoid")
@RequiredArgsConstructor
public class LaoIDAuthController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final MailProperties mailProperties;
    private final UserImapRepository userImapRepository;

    private static final String CLIENT_ID = "660dfa27-5a95-4c88-8a55-abe1310bf579";
    private static final String CLIENT_SECRET = "df1699140bcb456eaa6d85d54c5fbd79";

    @PostMapping
    public ApiResponse<AuthenticationResponse> handleLaoIdLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null) {
            throw new AppException(ErrorCode.INVALID_ENUM_KEY);
        }

        log.info(code);

        // Step 1: Gọi verify để lấy access token
        String accessToken = getAccessTokenFromLaoId(code);
        if (accessToken == null) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(444).build();
        }

        // Step 2: Gọi API lấy thông tin user
        Map<String, Object> userInfo = getUserInfoFromLaoId(accessToken);
        if (userInfo == null) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(444).build();
        }
        log.info("📦 LaoID userInfo: {}", userInfo);


        // Step 3: Lưu user vào DB nếu chưa có
        String email = extractEmail(userInfo);
        // ✅ Lấy thêm firstName và lastName
        String firstName = extractFirstName(userInfo);
        String lastName = extractLastName(userInfo);

        log.info("Email: {}, FirstName: {}, LastName: {}", email, firstName, lastName);
        if (email == null) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(444).build();
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Tài khoản chưa tồn tại, đang tạo mới cho email: {}", email);
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);

                    String dovecotPassword = Crypt.crypt(mailProperties.getSharedPassword(), "$6$" + UUID.randomUUID().toString().substring(0, 8));

                    UserIMAP userIMAP = new UserIMAP();
                    userIMAP.setEmail(email);
                    userIMAP.setPassword(dovecotPassword);

                    userImapRepository.save(userIMAP);

                    return userRepository.save(newUser);
                });

        LaoIDRequest laoIDRequest = new LaoIDRequest(user.getEmail());

        // Step 4: Sinh JWT từ hệ thống bạn
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate_LaoID(laoIDRequest))
                .build();
    }

    private String getAccessTokenFromLaoId(String code) {
        String url = "https://demo-sso.tinasoft.io/api/v1/third-party/verify";

        Map<String, String> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("clientId", CLIENT_ID);
        payload.put("clientSecret", CLIENT_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Accept-Language", "vi");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            log.info("📤 Đang gọi verify LaoID với code: {}", code);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            log.info("📥 Trả về từ verify: {}", response);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                log.info("📦 Body: {}", body);
                if (Boolean.TRUE.equals(body.get("success"))) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    return (String) data.get("accessToken");
                } else {
                    log.warn("❗Verify không thành công: {}", body);
                }
            } else {
                log.warn("❗HTTP trả về không thành công: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi verify từ LaoID", e);
        }

        return null;
    }


    private Map<String, Object> getUserInfoFromLaoId(String accessToken) {
        String url = "https://demo-sso.tinasoft.io/api/v1/third-party/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("x-api-key", CLIENT_ID);
        headers.set("X-Accept-Language", "vi");

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && Boolean.TRUE.equals(response.getBody().get("success"))) {
                return (Map<String, Object>) response.getBody().get("data");
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user từ LaoID", e);
        }

        return null;
    }

    private String extractEmail(Map<String, Object> userInfo) {
        try {
            List<Map<String, Object>> emails = (List<Map<String, Object>>) userInfo.get("email");
            return (String) emails.get(0).get("email");
        } catch (Exception e) {
            log.warn("Không thể extract email từ userInfo");
            return null;
        }
    }

    // ✅ Thêm hàm lấy firstName
    private String extractFirstName(Map<String, Object> userInfo) {
        try {
            return (String) userInfo.get("firstName");
        } catch (Exception e) {
            log.warn("Không thể extract firstName từ userInfo");
            return null;
        }
    }

    // ✅ Thêm hàm lấy lastName
    private String extractLastName(Map<String, Object> userInfo) {
        try {
            return (String) userInfo.get("lastName");
        } catch (Exception e) {
            log.warn("Không thể extract lastName từ userInfo");
            return null;
        }
    }
}