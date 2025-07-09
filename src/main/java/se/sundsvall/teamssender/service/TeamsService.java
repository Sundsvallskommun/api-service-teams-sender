package se.sundsvall.teamssender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TeamsService {

	private static final Logger logger = LoggerFactory.getLogger(TeamsService.class);
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Gets the Azure AD user ID for the given userPrincipalName (UPN/email).
	 */
	public String getUserId(String accessToken, String userPrincipalName) throws IOException {
		String url = "https://graph.microsoft.com/v1.0/users/" + userPrincipalName;
		HttpGet request = new HttpGet(url);
		request.setHeader("Authorization", "Bearer " + accessToken);
		System.out.println("Access token: " + accessToken);

		try (CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(request)) {

			int status = response.getCode();
			String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

			logger.debug("Response status: {}, body: {}", status, body);
			if (status >= 300) {
				throw new RuntimeException("Failed to get userId: HTTP " + status);
			}

			JsonNode json = mapper.readTree(body);
			return json.get("id").asText();
		}
	}

	/**
	 * Creates a one-on-one chat between two users and returns the chat ID.
	 */
	public String createOneOnOneChat(String accessToken, String user1Id, String user2Id) throws IOException {
		String url = "https://graph.microsoft.com/beta/chats";
		HttpPost request = new HttpPost(url);
		request.setHeader("Authorization", "Bearer " + accessToken);
		request.setHeader("Content-Type", "application/json");

		String jsonBody = """
			{
			  "chatType": "oneOnOne",
			  "members": [
			    {
			      "@odata.type": "#microsoft.graph.aadUserConversationMember",
			      "roles": ["owner"],
			      "user@odata.bind": "https://graph.microsoft.com/v1.0/users('%s')"
			    },
			    {
			      "@odata.type": "#microsoft.graph.aadUserConversationMember",
			      "roles": ["owner"],
			      "user@odata.bind": "https://graph.microsoft.com/v1.0/users('%s')"
			    }
			  ]
			}
			""".formatted(user1Id, user2Id);

		request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

		try (CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(request)) {

			int status = response.getCode();
			String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

			logger.debug("Response status: {}, body: {}", status, responseBody);

			if (status >= 300) {
				String errorBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
				throw new RuntimeException("Failed to create chat: HTTP " + status + ", body: " + errorBody);
			}

			JsonNode json = mapper.readTree(responseBody);
			return json.get("id").asText();
		}
	}

	/**
	 * Sends a chat message in the specified chat.
	 */
	public void sendMessage(String accessToken, String chatId, String messageText) throws IOException {
		String url = "https://graph.microsoft.com/beta/chats/" + chatId + "/messages";
		HttpPost request = new HttpPost(url);
		request.setHeader("Authorization", "Bearer " + accessToken);
		request.setHeader("Content-Type", "application/json");

		String jsonBody = """
			{
			  "body": {
			    "content": "%s"
			  }
			}
			""".formatted(messageText.replace("\"", "\\\"")); // Escape quotes in message

		logger.debug("POST {} with body: {}", url, jsonBody);
		request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

		try (CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(request)) {

			int status = response.getCode();
			if (status >= 300) {
				String errorBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
				throw new RuntimeException("Failed to send message: HTTP " + status + ", body: " + errorBody);
			}
			logger.debug("Message sent successfully with status: {}", status);
		}
	}
}
