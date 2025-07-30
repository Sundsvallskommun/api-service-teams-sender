package se.sundsvall.teamssender.service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.entity.OAuthSession;
import se.sundsvall.teamssender.repository.OAuthSessionRepository;

import java.time.Instant;
import java.util.*;
import java.util.List;


@Service
public class MicrosoftGraphTeamsSender {

	private GraphServiceClient graphClient;
	private final OAuthSessionRepository oAuthSessionRepository;
	private final TokenService tokenService;

	public MicrosoftGraphTeamsSender(OAuthSessionRepository oAuthSessionRepository, TokenService tokenService) {
		this.oAuthSessionRepository = oAuthSessionRepository;
		this.tokenService = tokenService;
	}


	// Väntar på token i upp till maxWaitMillis, pollar var pollIntervalMillis
	public synchronized void waitForAndInitializeClient(String userId, long maxWaitMillis, long pollIntervalMillis) throws InterruptedException {
		long waited = 0;
		while (waited < maxWaitMillis) {
			System.out.println("Waiting for access token for '" + userId +"'");
			Optional<OAuthSession> sessionOpt = oAuthSessionRepository.findByUserIdIgnoreCase(userId);

			if (sessionOpt.isPresent()) {
				OAuthSession session = sessionOpt.get();
				String accessToken = session.getAccessToken();
				System.out.println("AccessToken found: '" + accessToken + "'");

					if (accessToken != null && !accessToken.isEmpty()) {
						if (Instant.now().isAfter(session.getExpiresAt().minusSeconds(300))) {
							System.out.println("Access token is expired or about to expire. Refreshing...");
							accessToken = tokenService.getValidAccessToken(userId); // detta returnerar ny token
						} else {
							System.out.println("Access token is still valid.");
						}
						System.out.println("Initializing client with access token.");
						initializeClientWithAccessToken(accessToken); // 🟢

						return;
					} else {
						System.out.println("Access token is null or empty.");
					}
				} else {
					System.out.println("No OAuth session found for user.");
				}


			Thread.sleep(pollIntervalMillis);
			waited += pollIntervalMillis;
		}

		throw new IllegalStateException("Timeout waiting for access token for user " + userId);
	}

	public GraphServiceClient getGraphClient() {
		if (this.graphClient == null) {
			throw new IllegalStateException("Graph client is not initialized. Call initializeClient() with a valid token first.");
		}
		return this.graphClient;
	}

	public Chat createChat(String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		List<ConversationMember> members = new LinkedList<>();
		members.add(createMember(user1Id));
		members.add(createMember(user2Id));

		chat.setMembers(members);

		return getGraphClient().chats().post(chat);
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request) {
		Chat createdChat = createChat(request.getUser(), request.getSender());
		ChatMessage chatMessage = createMessage(request.getMessage());

		getGraphClient().chats()
				.byChatId(Objects.requireNonNull(createdChat.getId()))
				.messages()
				.post(chatMessage);
	}

	private ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContentType(BodyType.Text);
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private ConversationMember createMember(String userId) {
		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(Collections.singletonList("owner"));

		Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
		member.setAdditionalData(additionalData);

		return member;
	}


	private void initializeClientWithAccessToken(String accessToken) {
		TokenCredential authProvider = new SimpleAuthProvider(accessToken);
		this.graphClient = new GraphServiceClient(authProvider);

	}
}