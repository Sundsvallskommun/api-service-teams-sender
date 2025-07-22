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
	@Value("${azure.ad.redirecturi}")
	private String redirectUri;

	private final OAuthSessionRepository repo;

	public TokenService(OAuthSessionRepository repo) {
		this.repo = repo;
	}
	public void exchangeAuthorizationCodeForToken(String authorizationCode) {
		try {
			HttpClient client = HttpClient.newHttpClient();

			// Steg 1: Byt authorization code mot tokens
			String scopes = URLEncoder.encode("User.Read Chat.ReadWrite api://" + clientId + "/access_as_user", StandardCharsets.UTF_8);
			String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
					+ "&scope=" + scopes
					+ "&code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8)
					+ "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
					+ "&grant_type=authorization_code"
					+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

			HttpRequest tokenRequest = HttpRequest.newBuilder()
					.uri(URI.create("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.build();

			HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

			System.out.println("Token response status: " + tokenResponse.statusCode());
			System.out.println("Token response body: " + tokenResponse.body());

			if (tokenResponse.statusCode() != 200) {
				throw new RuntimeException("Token exchange failed: " + tokenResponse.body());
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode tokenJson = mapper.readTree(tokenResponse.body());

			JsonNode accessTokenNode = tokenJson.get("access_token");
			JsonNode refreshTokenNode = tokenJson.get("refresh_token");
			JsonNode expiresInNode = tokenJson.get("expires_in");

				// Kontrollera om fälten finns
			if (accessTokenNode == null || refreshTokenNode == null || expiresInNode == null) {
				System.err.println("Token response saknar ett eller flera obligatoriska fält: " + tokenResponse.body());
				throw new RuntimeException("Token-svaret saknar access_token, refresh_token eller expires_in");
			}

			String accessToken = accessTokenNode.asText();
			String refreshToken = refreshTokenNode.asText();
			int expiresIn = expiresInNode.asInt();


			// Steg 2: Hämta användarinformation via Graph API
			HttpRequest userInfoRequest = HttpRequest.newBuilder()
					.uri(URI.create("https://graph.microsoft.com/v1.0/me"))
					.header("Authorization", "Bearer " + accessToken)
					.GET()
					.build();

			HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

			if (userInfoResponse.statusCode() != 200) {
				throw new RuntimeException("Failed to fetch user info: " + userInfoResponse.body());
			}

			System.out.println("User info response: " + userInfoResponse.body());


			JsonNode userInfoJson = mapper.readTree(userInfoResponse.body());
			String userId = userInfoJson.get("userPrincipalName").asText(); // alternativt "userPrincipalName"

			System.out.println("User ID: " + userId);

			// Steg 3: Spara eller uppdatera session
			OAuthSession session = repo.findByUserId(userId).orElse(new OAuthSession());
			session.setUserId(userId);
			session.setAccessToken(accessToken);
			session.setRefreshToken(refreshToken);
			session.setExpiresAt(Instant.now().plusSeconds(expiresIn));

			repo.save(session);

		} catch (Exception e) {
			throw new RuntimeException("Failed to exchange authorization code for token", e);
		}
	}

	public synchronized String getValidAccessToken(String userId) {
		System.out.println("getValidAccessToken - Incoming userId: " + userId);

		Optional<OAuthSession> sessionOptional = repo.findByUserId(userId);

		if (sessionOptional.isEmpty()) {
			System.out.println("getValidAccessToken - No session found for userId: " + userId);
			throw new RuntimeException("No session for user " + userId);
		}

		OAuthSession session = sessionOptional.get();

		System.out.println("getValidAccessToken - Found session: " + session);

		Instant now = Instant.now();
		if (now.isAfter(session.getExpiresAt().minusSeconds(300))) {
			System.out.println("getValidAccessToken - Token expired or close to expiring, refreshing...");
			refreshToken(session);
		}

		return session.getAccessToken();
	}

	private void refreshToken(OAuthSession session) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			System.out.println("refreshToken - Refreshing for userId: " + session.getUserId());

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

			System.out.println("refreshToken - Response code: " + response.statusCode());
			System.out.println("refreshToken - Response body: " + response.body());

			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.body());

			String newAccessToken = json.get("access_token").asText();
			String newRefreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : session.getRefreshToken();
			int expiresIn = json.get("expires_in").asInt();

			session.setAccessToken(newAccessToken);
			session.setRefreshToken(newRefreshToken);
			session.setExpiresAt(Instant.now().plusSeconds(expiresIn));

			repo.save(session);

			System.out.println("refreshToken - Token refresh succeeded for userId: " + session.getUserId());


		} catch (Exception e) {
			throw new RuntimeException("Could not refresh token", e);
		}
	}
}
