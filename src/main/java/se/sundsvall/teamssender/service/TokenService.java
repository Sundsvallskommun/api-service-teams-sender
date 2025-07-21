package se.sundsvall.teamssender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.entity.OAuthSession;
import se.sundsvall.teamssender.repository.OAuthSessionRepository;

@Service
public class TokenService {

	@Value("${azure.ad.tenant-id}")
	private String tenantId;
	@Value("${azure.ad.client-id}")
	private String clientId;
	@Value ("${azure.ad.client-secret}")
	private String clientSecret;


	public TokenService(OAuthSessionRepository repo) {
		this.repo = repo;
	}
	public void exchangeAuthorizationCodeForToken(String authorizationCode, String redirectUri, String userId) {
		try {
			HttpClient client = HttpClient.newHttpClient();

			String scopes = URLEncoder.encode("User.Read Chat.ReadWrite api://" + clientId + "/access_as_user", StandardCharsets.UTF_8);
			String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
					+ "&scope=" + scopes
					+ "&code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8)
					+ "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
					+ "&grant_type=authorization_code"
					+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new RuntimeException("Token exchange failed: " + response.body());
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.body());

			String accessToken = json.get("access_token").asText();
			String refreshToken = json.get("refresh_token").asText();
			int expiresIn = json.get("expires_in").asInt();

			OAuthSession session = repo.findByUserId(userId).orElse(new OAuthSession());
			session.setUserId(userId);
			session.setAccessToken(accessToken);
			session.setRefreshToken(refreshToken);
			session.setExpiresAt(Instant.now().plusSeconds(expiresIn));

			repo.save(session);

		} catch (Exception e) {
			throw new RuntimeException("Failed to exchange authorization code for tokens", e);
		}
	}

	public synchronized String getValidAccessToken(String userId) {
		Optional<OAuthSession> sessionOptional = repo.findByUserId(userId);
		if (!sessionOptional.isPresent()) {
			throw new RuntimeException("No session for user " + userId);
		}
		OAuthSession session = sessionOptional.get();

		Instant now = Instant.now();
		if (now.isAfter(session.getExpiresAt().minusSeconds(300))) {
			refreshToken(session);
		}
		return session.getAccessToken();
	}

	private void refreshToken(OAuthSession session) {
		try {
			HttpClient client = HttpClient.newHttpClient();

			String scopes = URLEncoder.encode("User.Read Chat.ReadWrite api://" + clientId + "/access_as_user", StandardCharsets.UTF_8);
			String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
				+ "&scope=" + scopes
				+ "&refresh_token=" + URLEncoder.encode(session.getRefreshToken(), StandardCharsets.UTF_8)
				+ "&grant_type=refresh_token"
				+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.body());

			String newAccessToken = json.get("access_token").asText();
			String newRefreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : session.getRefreshToken();
			int expiresIn = json.get("expires_in").asInt();

			session.setAccessToken(newAccessToken);
			session.setRefreshToken(newRefreshToken);
			session.setExpiresAt(Instant.now().plusSeconds(expiresIn));

			repo.save(session);

		} catch (Exception e) {
			throw new RuntimeException("Could not refresh token", e);
		}
	}
}
