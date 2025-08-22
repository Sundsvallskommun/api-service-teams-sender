package se.sundsvall.teamssender.service;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.auth.service.TokenService;
import se.sundsvall.teamssender.exceptions.*;

@Service
public class TeamsSenderService {

	final TokenService tokenService;

	public TeamsSenderService(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request, String municipalityId) {
		GraphServiceClient graphClient;
		try {
			graphClient = tokenService.initializeGraphServiceClient(municipalityId);
		} catch (Exception e) {
			throw new GraphConnectionException("Failed to initialize GraphServiceClient for municipality " + municipalityId, e);
		}

		if (graphClient == null) {
			throw new GraphConnectionException("GraphServiceClient could not be initialized for municipality " + municipalityId);
		}

		User sender;
		try {
			sender = graphClient.me().get();
		} catch (Exception e) {
			throw new AuthenticationException("Could not resolve sender from Graph API", e);
		}

		if (sender == null) {
			throw new AuthenticationException("Sender was null when fetched from Graph API");
		}

		Chat createdChat = createChat(graphClient, sender.getUserPrincipalName(), request.getRecipient());
		ChatMessage chatMessage = createMessage(request.getMessage());

		try {
			graphClient.chats()
				.byChatId(createdChat.getId())
				.messages()
				.post(chatMessage);
		} catch (Exception e) {
			throw new MessageSendException("Failed to send message to recipient " + request.getRecipient(), e);
		}
	}

	public Chat createChat(GraphServiceClient graphClient, String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(graphClient, user1Id));
		members.add(createMember(graphClient, user2Id));
		chat.setMembers(members);

		Chat createdChat;
		try {
			createdChat = graphClient.chats().post(chat);
		} catch (Exception e) {
			throw new ChatNotCreatedException("Failed to create chat between " + user1Id + " and " + user2Id, e);
		}

		if (createdChat == null || createdChat.getId() == null) {
			throw new ChatNotCreatedException("Chat creation returned null for users: " + user1Id + " and " + user2Id);
		}

		return createdChat;
	}

	public ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	public AadUserConversationMember createMember(GraphServiceClient graphClient, String userEmail) {
		User user;
		try {
			user = graphClient.users().byUserId(userEmail).get();
		} catch (Exception e) {
			throw new RecipientException("Failed to fetch recipient " + userEmail + " from Graph API", e);
		}

		if (user == null || user.getId() == null) {
			throw new RecipientException("Recipient with email " + userEmail + " not found in Graph API");
		}

		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + user.getId() + "')");
		member.setAdditionalData(additionalData);

		return member;
	}
}
