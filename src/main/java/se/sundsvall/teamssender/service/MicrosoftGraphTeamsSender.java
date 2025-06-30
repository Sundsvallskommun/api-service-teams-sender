package se.sundsvall.teamssender.service;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import se.sundsvall.teamssender.api.model.SendTeamsMessageRequest;

public class MicrosoftGraphTeamsSender implements TeamsSender {

	private final GraphServiceClient graphClient;
	private String municipalityId;


	public MicrosoftGraphTeamsSender(GraphServiceClient graphClient) {
		this.graphClient = graphClient;
	}

	@Override
	public String getMunicipalityId() {
		return municipalityId;
	}

	@Override
	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public Chat createChat(String user1Id, String user2Id) {
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(user1Id));
		members.add(createMember(user2Id));
		chat.setMembers(members);

		return graphClient.chats().post(chat);
	}

	public void sendTeamsMessage(SendTeamsMessageRequest request) {
		var createdChat = createChat(request.getUser(), request.getSender());
		var chatMessage = createMessage(request.getMessage());

		graphClient.chats()
			.byChatId(Objects.requireNonNull(createdChat.getId()))
			.messages()
			.post(chatMessage);
	}

	public ChatMessage createMessage(String message) {
		ItemBody body = new ItemBody();
		body.setContent(message);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(body);

		return chatMessage;
	}

	private AadUserConversationMember createMember(String userId) {
		AadUserConversationMember member = new AadUserConversationMember();
		member.setOdataType("#microsoft.graph.aadUserConversationMember");
		member.setRoles(List.of("owner"));

		HashMap<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
		member.setAdditionalData(additionalData);

		return member;
	}
}
