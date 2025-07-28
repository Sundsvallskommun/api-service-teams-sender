package se.sundsvall.teamssender.service;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;
import se.sundsvall.teamssender.auth.service.TokenService;

@Service
public class TeamsSenderService {

	@Value("${teams.sender}")
	private String systemUser;

	private final TokenService tokenService;

	@Autowired
	public TeamsSenderService(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request) throws Exception {
		GraphServiceClient graphClient = tokenService.initializeGraphServiceClient();

		var createdChat = createChat(graphClient, systemUser, request.getRecipient());
		var chatMessage = createMessage(request.getMessage());

		graphClient.chats()
			.byChatId(Objects.requireNonNull(createdChat.getId()))
			.messages()
			.post(chatMessage);
	}

	public Chat createChat(GraphServiceClient graphClient, String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(graphClient, user1Id));
		members.add(createMember(graphClient, user2Id));
		chat.setMembers(members);

		return graphClient.chats().post(chat);
	}

	public ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private AadUserConversationMember createMember(GraphServiceClient graphClient, String userEmail) {
		User user = graphClient.users().byUserId(userEmail).get();
		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + user.getId() + "')");
		member.setAdditionalData(additionalData);

		return member;
	}
}
