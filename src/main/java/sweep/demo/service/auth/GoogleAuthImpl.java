package sweep.demo.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAuthImpl implements GoogleAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${google.redirect.url}")
    private String GOOGLE_REDIRECT_URL;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 구글 code로 access token 요청
    @Override
    public String getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("client_id", GOOGLE_CLIENT_ID);
        requestBody.put("client_secret", GOOGLE_CLIENT_SECRET);
        requestBody.put("redirect_uri", GOOGLE_REDIRECT_URL);
        requestBody.put("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("구글 Access Token 요청 실패", e);
        }
    }

    // 2. Access Token으로 구글 사용자 정보 가져오기
    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            System.out.println("[GoogleAuthImpl] 구글 응답: " + response.getBody());
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("socialId", jsonNode.get("id").asText());
            userInfo.put("email", jsonNode.get("email").asText());
            userInfo.put("username", jsonNode.get("name").asText());
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("구글 사용자 정보 요청 실패", e);
        }
    }
}
