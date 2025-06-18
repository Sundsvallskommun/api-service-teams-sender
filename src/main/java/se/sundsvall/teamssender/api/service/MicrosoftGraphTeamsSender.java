package se.sundsvall.teamssender.api.service;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MicrosoftGraphTeamsSender {

	private final GraphServiceClient graphClient;
	private String municipalityId;

	public MicrosoftGraphTeamsSender(GraphServiceClient graphClient) {
		this.graphClient = graphClient;
	}

	public void createChat(String user1Id, String user2Id) {
		// 1. Skapa chat
		Chat chat = new Chat();
		chat.setChatType(ChatType.OneOnOne);

		LinkedList<ConversationMember> members = new LinkedList<>();
		members.add(createMember(user1Id));
		members.add(createMember(user2Id));
		chat.setMembers(members);

		Chat createdChat = graphClient.chats().post(chat);

	}

	public void sendMessage(Chat createdChat, ChatMessage chatMessage) {
		graphClient.chats()
			.byChatId(createdChat.getId())
			.messages()
			.post(chatMessage);
	}

	public void createMessage(String message) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setBody(new ItemBody() {
			{
				setContent(message);
			}
		});
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
