package se.sundsvall.teamssender.service;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.RequestAdapter;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import com.microsoft.graph.authenticationmethodconfigurations.AuthenticationMethodConfigurationsRequestBuilder;
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
public class MicrosoftGraphTeamsSender {
	@Value("${azure.ad.tenant-id}")
	private String tenantId;

	@Value("${azure.ad.client-id}")
	private String clientId;

	@Value("${azure.ad.certificate-path}")
	private String certificatePath; // path to .pfx or .pem

	@Value("${azure.ad.certificate-key}")
	private String certificateKey; //
	@Value ("${azure.ad.client-secret}")
	private String clientSecret;

	private final GraphServiceClient graphClient;
	private final OAuthSessionRepository oAuthSessionRepository;

	public MicrosoftGraphTeamsSender(OAuthSessionRepository oAuthSessionRepository) {
		this.oAuthSessionRepository = oAuthSessionRepository;

		// Exempel på init
		AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
				.clientId(clientId)
				.tenantId(tenantId)
				.clientSecret(clientSecret)
				.authorizationCode("authCode")
				.redirectUrl("http://localhost:8080/")
				.build();

			this.graphClient = new GraphServiceClient(credential);
	}

	public GraphServiceClient getGraphClient() {
		return graphClient;
	}

	public Chat createChat(String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		List<ConversationMember> members = new LinkedList<>();
		members.add(createMember(user1Id));
		members.add(createMember(user2Id));

		chat.setMembers(members);

		return graphClient.chats().post(chat);
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request) {
		Chat createdChat = createChat(request.getUser(), request.getSender());
		ChatMessage chatMessage = createMessage(request.getMessage());

		graphClient.chats()
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