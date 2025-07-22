package se.sundsvall.teamssender.service;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.entity.OAuthSession;
import se.sundsvall.teamssender.repository.OAuthSessionRepository;

import java.util.*;
import java.util.List;

//package se.sundsvall.teamssender.service;
//
//import com.microsoft.graph.models.*;
//import com.microsoft.graph.serviceclient.GraphServiceClient;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//
//public abstract class MicrosoftGraphTeamsSender implements TeamsSender {
//
//	private final GraphServiceClient graphClient;
//	private String municipalityId;
//
//	public MicrosoftGraphTeamsSender(GraphServiceClient graphClient) {
//		this.graphClient = graphClient;
//	}
//
//	@Override
//	public String getMunicipalityId() {
//		return municipalityId;
//	}
//
//	@Override
//	public void setMunicipalityId(final String municipalityId) {
//		this.municipalityId = municipalityId;
//	}
//
//	public Chat createChat(String user1Id, String user2Id) {
//		Chat chat = new Chat();
//		chat.setChatType(ChatType.OneOnOne);
//
//		LinkedList<ConversationMember> members = new LinkedList<>();
//		members.add(createMember(user1Id));
//		members.add(createMember(user2Id));
//		chat.setMembers(members);
//
//		return graphClient.chats().post(chat);
//	}
//
//	public void sendTeamsMessage(SendTeamsMessageRequest request) {
//		var createdChat = createChat(request.getUser(), request.getSender());
//		var chatMessage = createMessage(request.getMessage());
//
//		graphClient.chats()
//			.byChatId(Objects.requireNonNull(createdChat.getId()))
//			.messages()
//			.post(chatMessage);
//	}
//
//	public ChatMessage createMessage(String message) {
//		ItemBody body = new ItemBody();
//		body.setContent(message);
//
//		ChatMessage chatMessage = new ChatMessage();
//		chatMessage.setBody(body);
//
//		return chatMessage;
//	}
//
//	private AadUserConversationMember createMember(String userId) {
//		AadUserConversationMember member = new AadUserConversationMember();
//		member.setOdataType("#microsoft.graph.aadUserConversationMember");
//		member.setRoles(List.of("owner"));
//
//		HashMap<String, Object> additionalData = new HashMap<>();
//		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
//		member.setAdditionalData(additionalData);
//
//		return member;
//	}
//}
@Service
public class MicrosoftGraphTeamsSender {
	@Value("${azure.ad.tenant-id}")
	private String tenantId;

	@Value("${azure.ad.client-id}")
	private String clientId;

	@Value ("${azure.ad.client-secret}")
	private String clientSecret;



	private GraphServiceClient graphClient;
	private final OAuthSessionRepository oAuthSessionRepository;

	public MicrosoftGraphTeamsSender(OAuthSessionRepository oAuthSessionRepository) {
		this.oAuthSessionRepository = oAuthSessionRepository;
	}

	// Väntar på token i upp till maxWaitMillis, pollar var pollIntervalMillis
	public synchronized void waitForAndInitializeClient(String userId, long maxWaitMillis, long pollIntervalMillis) throws InterruptedException {
		long waited = 0;
		while (waited < maxWaitMillis) {
			System.out.println("Waiting for client " + userId);
			Optional<OAuthSession> sessionOpt = oAuthSessionRepository.findByUserId(userId); // ✅ korrekt

			if (sessionOpt.isPresent()) {
				String code = sessionOpt.get().getAuthorizationCode(); // ✅ korrekt
				if (code != null && !code.isEmpty()) {
					initializeClient(code);
					return;
				}
			}

			Thread.sleep(pollIntervalMillis);
			waited += pollIntervalMillis;
		}

		throw new IllegalStateException("Timeout waiting for authorization code for user " + userId);
	}

	public synchronized void initializeClient(String authorizationCode) {
		if (this.graphClient == null) {
			AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
					.clientId(clientId)
					.tenantId(tenantId)
					.clientSecret(clientSecret)
					.authorizationCode(authorizationCode)
					.redirectUrl("http://localhost:8080/callback")
					.build();

			this.graphClient = new GraphServiceClient(credential);
		}
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
}