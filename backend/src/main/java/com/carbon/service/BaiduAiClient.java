package com.carbon.service;

import com.carbon.config.CarbonProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Service
public class BaiduAiClient {
    private final RestTemplate restTemplate;
    private final CarbonProperties carbonProperties;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant accessTokenExpiresAt;

    public BaiduAiClient(RestTemplate restTemplate, CarbonProperties carbonProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.carbonProperties = carbonProperties;
        this.objectMapper = objectMapper;
    }

    public AiLabelScore classify(String base64Image) {
        String token = getAccessToken();
        if ("MOCK_TOKEN".equals(token)) {
            System.out.println("[BaiduAiClient] Baidu AI credentials not configured. Falling back to MOCK mode!");
            return new AiLabelScore("Mock低碳环保识别", 0.99);
        }

        String endpoint = carbonProperties.getAi().getBaidu().getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("Baidu AI endpoint is not configured");
        }

        String body = "image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String url = endpoint + "?access_token=" + token;
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        JsonNode root = readJson(response.getBody());
        
        // 错误信息拦截与友好提示
        if (root.has("error_code")) {
            int errorCode = root.path("error_code").asInt();
            String errorMsg = root.path("error_msg").asText();
            System.err.println("[BaiduAiClient] Baidu AI returned error_code: " + errorCode + ", msg: " + errorMsg);
            throw new IllegalStateException("Baidu AI error: [" + errorCode + "] " + errorMsg);
        }

        // 自适应兼容「图像描述/看图识万物」与「通用物体和场景识别」
        if (endpoint.contains("caption") || endpoint.contains("understanding") || root.path("result").isObject()) {
            JsonNode resultNode = root.path("result");
            if (resultNode.isObject() && resultNode.has("description")) {
                String description = resultNode.path("description").asText("unknown");
                return new AiLabelScore(description, 0.99); // 图像描述整体置信度默认为高可信度
            }
        }

        // 默认通用识别提取
        JsonNode resultNode = root.path("result");
        if (!resultNode.isArray() || resultNode.isEmpty()) {
            return new AiLabelScore("unknown", 0.0);
        }

        JsonNode first = resultNode.get(0);
        String label = first.path("keyword").asText("unknown");
        double score = first.path("score").asDouble(0.0);
        return new AiLabelScore(label, score);
    }

    private String getAccessToken() {
        String apiKey = carbonProperties.getAi().getBaidu().getApiKey();
        String secretKey = carbonProperties.getAi().getBaidu().getSecretKey();
        if (apiKey == null || apiKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            return "MOCK_TOKEN";
        }

        if (accessToken != null && accessTokenExpiresAt != null && accessTokenExpiresAt.isAfter(Instant.now().plusSeconds(60))) {
            return accessToken;
        }

        String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8);

        String response = restTemplate.getForObject(tokenUrl, String.class);
        JsonNode root = readJson(response);
        String token = root.path("access_token").asText();
        int expiresIn = root.path("expires_in").asInt(0);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Failed to acquire Baidu AI access token");
        }
        accessToken = token;
        accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(expiresIn, 0));
        return accessToken;
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(Objects.requireNonNullElse(body, "{}"));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Baidu AI response", ex);
        }
    }

    public record AiLabelScore(String label, double score) {
    }
}
