package se.sundsvall.teamssender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OboTokenService {

    @Value("${azure.ad.tenant-id}")
    private String tenantId;

    @Value("${azure.ad.client-id}")
    private String clientId;

    @Value("${azure.ad.client-secret}")
    private String clientSecret;

    private String tokenUrl;
    private final String scope = "https://graph.microsoft.com/.default";

    private final ObjectMapper mapper = new ObjectMapper();

    // Thread-safe token cache per user
    private final Map<String, TokenResponse> tokenCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
    }

    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public long expiresAt; // millis epoch

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Exchange frontend user token for MS Graph token + refresh token using OBO.
     * Cache the token per userId.
     */
    public TokenResponse acquireOboToken(String userAccessToken, String userId) throws Exception {
        HttpPost post = new HttpPost(tokenUrl);
        post.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());

        List<NameValuePair> params = List.of(
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"),
                new BasicNameValuePair("requested_token_use", "on_behalf_of"),
                new BasicNameValuePair("scope", scope),
                new BasicNameValuePair("assertion", userAccessToken)
        );

        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {

            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            if (response.getCode() != 200) {
                throw new RuntimeException("OBO token request failed: " + body);
            }

            JsonNode json = mapper.readTree(body);

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.accessToken = json.get("access_token").asText();
            tokenResponse.refreshToken = json.get("refresh_token").asText();
            int expiresIn = json.get("expires_in").asInt();
            tokenResponse.expiresAt = System.currentTimeMillis() + (expiresIn - 60) * 1000L;

            tokenCache.put(userId, tokenResponse);
            return tokenResponse;
        }
    }

    /**
     * Get a valid access token for userId, refresh if expired.
     */
    public String getAccessTokenForUser(String userId) throws Exception {
        TokenResponse tokenResponse = tokenCache.get(userId);
        if (tokenResponse == null) {
            throw new IllegalStateException("No cached token for user: " + userId);
        }
        if (tokenResponse.isExpired()) {
            tokenResponse = refreshAccessToken(tokenResponse.refreshToken, userId);
            tokenCache.put(userId, tokenResponse);
        }
        return tokenResponse.accessToken;
    }

    private TokenResponse refreshAccessToken(String refreshToken, String userId) throws Exception {
        HttpPost post = new HttpPost(tokenUrl);
        post.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());

        List<NameValuePair> params = List.of(
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("grant_type", "refresh_token"),
                new BasicNameValuePair("refresh_token", refreshToken),
                new BasicNameValuePair("scope", scope)
        );

        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {

            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            if (response.getCode() != 200) {
                throw new RuntimeException("Refresh token request failed: " + body);
            }

            JsonNode json = mapper.readTree(body);

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.accessToken = json.get("access_token").asText();
            tokenResponse.refreshToken = json.get("refresh_token").asText();
            int expiresIn = json.get("expires_in").asInt();
            tokenResponse.expiresAt = System.currentTimeMillis() + (expiresIn - 60) * 1000L;

            return tokenResponse;
        }
    }
}